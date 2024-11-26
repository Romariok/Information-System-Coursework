package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import itmo.is.cw.GuitarMatchIS.dto.AuthResponseDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserDTO;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserAlreadyExistException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final JwtUtils jwtUtils;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

        public AuthResponseDTO register(UserDTO registerUserDto) {
                if (userRepository.existsByUsername(registerUserDto.getUsername()))
                        throw new UserAlreadyExistException(
                                        String.format("Username %s already exists", registerUserDto.getUsername()));

                User user = User
                                .builder()
                                .username(registerUserDto.getUsername())
                                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                                .isAdmin(false)
                                .createdAt(LocalDateTime.now())
                                .subscriptions(0)
                                .build();

                user = userRepository.save(user);

                String token = jwtUtils.generateJwtToken(user.getUsername());
                return new AuthResponseDTO(
                                user.getUsername(),
                                user.getIsAdmin(),
                                user.getSubscriptions(),
                                user.getCreatedAt(),
                                token);
        }

        public AuthResponseDTO login(UserDTO loginUserDto) {
                User user = userRepository.findByUsername(loginUserDto.getUsername())
                                .orElseThrow(() -> new UserNotFoundException(
                                                String.format("Username %s not found", loginUserDto.getUsername())));

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(loginUserDto.getUsername(),
                                                loginUserDto.getPassword()));

                String token = jwtUtils.generateJwtToken(user.getUsername());
                return new AuthResponseDTO(
                                user.getUsername(),
                                user.getIsAdmin(),
                                user.getSubscriptions(),
                                user.getCreatedAt(),
                                token);
        }

}
