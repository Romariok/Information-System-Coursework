package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import itmo.is.cw.GuitarMatchIS.dto.AuthResponseDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserDTO;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserAlreadyExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final JwtUtils jwtUtils;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;

        public AuthResponseDTO register(UserDTO registerUserDto) {
                log.info("Registering user with username: {}", registerUserDto.getUsername());
                if (userRepository.existsByUsername(registerUserDto.getUsername())) {
                        log.warn("User with username {} already exists", registerUserDto.getUsername());
                        throw new UserAlreadyExistException(
                                        String.format("Username %s already exists", registerUserDto.getUsername()));
                }
                User user = User
                                .builder()
                                .username(registerUserDto.getUsername())
                                .password(passwordEncoder.encode(registerUserDto.getPassword()))
                                .isAdmin(false)
                                .createdAt(LocalDateTime.now())
                                .subscriptions(0)
                                .build();

                user = userRepository.save(user);
                log.info("User with username {} successfully registered", user.getUsername());
                String token = jwtUtils.generateJwtToken(user.getUsername());
                return new AuthResponseDTO(
                                user.getUsername(),
                                user.getIsAdmin(),
                                user.getSubscriptions(),
                                user.getCreatedAt(),
                                token);
        }

        public AuthResponseDTO login(UserDTO loginUserDto) {
                log.info("Logging in user with username: {}", loginUserDto.getUsername());

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginUserDto.getUsername(),
                                                loginUserDto.getPassword()));

                User user = (User) authentication.getPrincipal();

                String token = jwtUtils.generateJwtToken(user.getUsername());
                log.info("User with username {} successfully logged in", user.getUsername());
                return new AuthResponseDTO(
                                user.getUsername(),
                                user.getIsAdmin(),
                                user.getSubscriptions(),
                                user.getCreatedAt(),
                                token);
        }

}
