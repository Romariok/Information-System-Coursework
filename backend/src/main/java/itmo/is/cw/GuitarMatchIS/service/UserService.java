package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.models.Role;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
        private final UserRepository userRepository;

        public Role getRoleByUsername(String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("Username %s not found", username)));
                return user.getIsAdmin() ? Role.ADMIN : Role.USER;
        }

}
