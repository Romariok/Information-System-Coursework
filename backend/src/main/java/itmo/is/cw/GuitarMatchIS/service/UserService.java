package itmo.is.cw.GuitarMatchIS.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Role;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.UserGenre;
import itmo.is.cw.GuitarMatchIS.models.UserTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
        private final UserRepository userRepository;
        private final UserGenreRepository userGenreRepository;
        private final UserTypeOfMusicianRepository userTypeOfMusicianRepository;
        private final JwtUtils jwtUtils;

        public Role getRoleByUsername(String username) {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("Username %s not found", username)));
                return user.getIsAdmin() ? Role.ADMIN : Role.USER;
        }

        public List<Genre> getGenresByUser(HttpServletRequest request) {
                User user = findUserByRequest(request);
                return userGenreRepository.findByUser(user).stream().map(UserGenre::getGenre).toList();
        }

        @Transactional
        public Boolean setGenresToUser(HttpServletRequest request, List<Genre> genres) {
                User user = findUserByRequest(request);
                userGenreRepository.deleteAllByUser(user.getId());
                genres.forEach(genre -> userGenreRepository.saveByUserIdAndGenre(user.getId(), genre.getCapsValue()));
                return true;
        }

        @Transactional
        public Boolean setTypesOfMusiciansToUser(HttpServletRequest request, List<TypeOfMusician> typesOfMusicians) {
                User user = findUserByRequest(request);
                userTypeOfMusicianRepository.deleteAllByUser(user.getId());
                typesOfMusicians.forEach(typeOfMusician -> userTypeOfMusicianRepository
                                .saveByUserIdAndTypeOfMusician(user.getId(), typeOfMusician.getCapsValue()));
                return true;
        }

        public List<TypeOfMusician> getTypesOfMusiciansByUser(HttpServletRequest request) {
                User user = findUserByRequest(request);
                return userTypeOfMusicianRepository.findByUser(user).stream().map(UserTypeOfMusician::getTypeOfMusician)
                                .toList();
        }

        private User findUserByRequest(HttpServletRequest request) {
                String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("Username %s not found", username)));
        }

}
