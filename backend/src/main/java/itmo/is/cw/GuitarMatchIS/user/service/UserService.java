package itmo.is.cw.GuitarMatchIS.user.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.user.dao.UserRepository;
import itmo.is.cw.GuitarMatchIS.user.model.Role;
import itmo.is.cw.GuitarMatchIS.user.model.User;
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
