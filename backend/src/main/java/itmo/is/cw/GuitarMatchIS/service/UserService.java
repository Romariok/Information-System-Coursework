package itmo.is.cw.GuitarMatchIS.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itmo.is.cw.GuitarMatchIS.dto.AddUserProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.MusicianProduct;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.Role;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.UserGenre;
import itmo.is.cw.GuitarMatchIS.models.UserMusician;
import itmo.is.cw.GuitarMatchIS.models.UserProduct;
import itmo.is.cw.GuitarMatchIS.models.UserTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductMusicianAlreadyExists;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
        private final UserRepository userRepository;
        private final UserGenreRepository userGenreRepository;
        private final UserTypeOfMusicianRepository userTypeOfMusicianRepository;
        private final UserProductRepository userProductRepository;
        private final ProductRepository productRepository;
        private final JwtUtils jwtUtils;
        private final UserMusicianRepository userMusicianRepository;
        private final MusicianGenreRepository musicianGenreRepository;
        private final MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
        private final MusicianProductRepository musicianProductRepository;

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

        public List<ProductDTO> getUserProducts(HttpServletRequest request) {
                User user = findUserByRequest(request);

                List<UserProduct> userProducts = userProductRepository.findByUser(user);

                return userProducts.stream().map(UserProduct::getProduct).map(this::convertToDTO).toList();
        }

        public List<MusicianInfoDTO> getSubscribedMusicians(HttpServletRequest request) {
                User user = findUserByRequest(request);
                List<Musician> musicians = userMusicianRepository.findByUser(user).stream()
                                .map(UserMusician::getMusician).toList();

                return musicians
                                .stream()
                                .map(musician1 -> convertToDTO(musician1,
                                                musicianGenreRepository.findByMusician(musician1),
                                                musicianTypeOfMusicianRepository.findByMusician(musician1),
                                                musicianProductRepository.findByMusician(musician1)))
                                .toList();
        }

        @Transactional
        public Boolean addProductToUser(AddUserProductDTO addUserProductDTO, HttpServletRequest request) {
                if (!productRepository.existsById(addUserProductDTO.getProductId()))
                        throw new ProductNotFoundException(
                                        "Product with id %s not found".formatted(addUserProductDTO.getProductId()));

                User user = findUserByRequest(request);

                Product product = productRepository.findById(addUserProductDTO.getProductId()).get();

                if (userProductRepository.existsByUserAndProduct(user, product))
                        throw new ProductMusicianAlreadyExists(
                                        "Product %s already exists in user %s".formatted(product.getName(),
                                                        user.getUsername()));

                userProductRepository
                                .save(UserProduct.builder().userId(user.getId()).productId(product.getId())
                                                .build());
                return true;
        }

        @Transactional
        public Boolean deleteProductFromUser(AddUserProductDTO addUserProductDTO, HttpServletRequest request) {
                if (!productRepository.existsById(addUserProductDTO.getProductId()))
                        throw new ProductNotFoundException(
                                        "Product with id %s not found".formatted(addUserProductDTO.getProductId()));

                User user = findUserByRequest(request);

                Product product = productRepository.findById(addUserProductDTO.getProductId()).get();

                if (!userProductRepository.existsByUserAndProduct(user, product))
                        throw new ProductNotFoundException(
                                        "Product %s not found in user %s".formatted(product.getName(),
                                                        user.getUsername()));

                userProductRepository.deleteByUserAndProduct(user, product);
                return true;
        }

        public UserInfoDTO getUserInfoById(Long id) {
                User user = userRepository.findById(id)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("User with id %s not found", id)));
                return UserInfoDTO.builder().id(user.getId()).username(user.getUsername()).build();
        }

        private User findUserByRequest(HttpServletRequest request) {
                String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
                return userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                String.format("Username %s not found", username)));
        }

        private ProductDTO convertToDTO(Product product) {
                return ProductDTO.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .rate(product.getRate())
                                .brand(BrandDTO.builder()
                                                .id(product.getBrand().getId())
                                                .name(product.getBrand().getName())
                                                .country(product.getBrand().getCountry())
                                                .website(product.getBrand().getWebsite())
                                                .email(product.getBrand().getEmail())
                                                .build())
                                .guitarForm(product.getGuitarForm())
                                .typeOfProduct(product.getTypeOfProduct())
                                .lads(product.getLads())
                                .avgPrice(product.getAvgPrice())
                                .color(product.getColor())
                                .strings(product.getStrings())
                                .tipMaterial(product.getTipMaterial())
                                .bodyMaterial(product.getBodyMaterial())
                                .pickupConfiguration(product.getPickupConfiguration())
                                .typeComboAmplifier(product.getTypeComboAmplifier())
                                .build();
        }

        private MusicianInfoDTO convertToDTO(Musician musician, List<MusicianGenre> musicianGenres,
                        List<MusicianTypeOfMusician> musicianTypes, List<MusicianProduct> musicianProducts) {
                return MusicianInfoDTO.builder()
                                .id(musician.getId())
                                .name(musician.getName())
                                .subscribers(musician.getSubscribers())
                                .genres(musicianGenres.stream().map(MusicianGenre::getGenre).toList())
                                .typesOfMusicians(musicianTypes.stream().map(MusicianTypeOfMusician::getTypeOfMusician)
                                                .toList())
                                .products(musicianProducts.stream().map(MusicianProduct::getProduct)
                                                .map(this::convertToDTO).toList())
                                .build();
        }

}
