package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.AuthResponseDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserDTO;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserAlreadyExistException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success() {
        User savedUser = TestDataFactory.buildUser();
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("generated-token");

        AuthResponseDTO result = authService.register(new UserDTO("testuser", "pass123"));

        verify(userRepository, times(1)).save(any(User.class));
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("generated-token");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void register_duplicateUsername_throwsUserAlreadyExistException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(UserAlreadyExistException.class,
                () -> authService.register(new UserDTO("testuser", "pass123")));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_nullPassword_throwsNullPointerException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(null)).thenThrow(NullPointerException.class);

        assertThrows(NullPointerException.class,
                () -> authService.register(new UserDTO("testuser", null)));
    }

    @Test
    void register_emptyUsername_serviceDoesNotValidate() {
        User savedUser = TestDataFactory.buildUser();
        when(userRepository.existsByUsername("")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateJwtToken(anyString())).thenReturn("token");

        // The service itself does not validate blank usernames; validation is at controller level
        AuthResponseDTO result = authService.register(new UserDTO("", "pass123"));

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_success() {
        User user = TestDataFactory.buildUser();
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateJwtToken("testuser")).thenReturn("login-token");

        AuthResponseDTO result = authService.login(new UserDTO("testuser", "pass123"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("login-token");
        assertThat(result.getUsername()).isEqualTo("testuser");
    }

    @Test
    void login_badCredentials_throwsBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(new UserDTO("testuser", "wrong-password")));
    }
}
