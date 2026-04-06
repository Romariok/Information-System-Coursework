package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.AddUserProductDTO;
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
import itmo.is.cw.GuitarMatchIS.repository.*;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductMusicianAlreadyExists;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGenreRepository userGenreRepository;

    @Mock
    private UserTypeOfMusicianRepository userTypeOfMusicianRepository;

    @Mock
    private UserProductRepository userProductRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMusicianRepository userMusicianRepository;

    @Mock
    private MusicianGenreRepository musicianGenreRepository;

    @Mock
    private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;

    @Mock
    private MusicianProductRepository musicianProductRepository;

    @InjectMocks
    private UserService userService;

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void getUserRole_userNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getRoleByUsername("unknown"));
    }

    @Test
    void getUserRole_regularUser_returnsUserRole() {
        User user = TestDataFactory.buildUser();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Role role = userService.getRoleByUsername("testuser");

        assertThat(role).isEqualTo(Role.USER);
    }

    @Test
    void getUserRole_adminUser_returnsAdminRole() {
        User admin = TestDataFactory.buildAdmin();
        when(userRepository.findByUsername("adminuser")).thenReturn(Optional.of(admin));

        Role role = userService.getRoleByUsername("adminuser");

        assertThat(role).isEqualTo(Role.ADMIN);
    }

    @Test
    void addUserProduct_alreadyAdded_throwsProductMusicianAlreadyExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        assertThrows(ProductMusicianAlreadyExists.class,
                () -> userService.addProductToUser(new AddUserProductDTO(1L), request));
    }

    @Test
    void addUserProduct_productNotFound_throwsProductNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> userService.addProductToUser(new AddUserProductDTO(99L), request));
    }

    @Test
    void deleteUserProduct_notFound_throwsProductNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> userService.deleteProductFromUser(new AddUserProductDTO(1L), request));
    }

    @Test
    void addUserProduct_success_savesLink() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        Boolean result = userService.addProductToUser(new AddUserProductDTO(1L), request);

        assertThat(result).isTrue();
        verify(userProductRepository, times(1)).save(any(UserProduct.class));
    }

    @Test
    void deleteUserProduct_success_deletesLink() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userProductRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        Boolean result = userService.deleteProductFromUser(new AddUserProductDTO(1L), request);

        assertThat(result).isTrue();
        verify(userProductRepository, times(1)).deleteByUserAndProduct(user, product);
    }

    @Test
    void getSubscribedMusicians_success_returnsDtos() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(userMusicianRepository.findByUser(user)).thenReturn(List.of(
                UserMusician.builder().userId(user.getId()).musicianId(musician.getId()).musician(musician).build()));

        when(musicianGenreRepository.findByMusicianIdIn(List.of(musician.getId())))
                .thenReturn(List.of(MusicianGenre.builder()
                        .musicianId(musician.getId())
                        .musician(musician)
                        .genre(Genre.ROCK)
                        .build()));
        when(musicianTypeOfMusicianRepository.findByMusicianIdIn(List.of(musician.getId())))
                .thenReturn(List.of(MusicianTypeOfMusician.builder()
                        .musicianId(musician.getId())
                        .musician(musician)
                        .typeOfMusician(TypeOfMusician.GUITARIST)
                        .build()));
        when(musicianProductRepository.findByMusicianIdIn(List.of(musician.getId())))
                .thenReturn(List.of(MusicianProduct.builder()
                        .musicianId(musician.getId())
                        .productId(1L)
                        .musician(musician)
                        .product(TestDataFactory.buildProduct())
                        .build()));

        var result = userService.getSubscribedMusicians(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(musician.getId());
        assertThat(result.get(0).getGenres()).containsExactly(Genre.ROCK);
        assertThat(result.get(0).getTypesOfMusicians()).containsExactly(TypeOfMusician.GUITARIST);
    }

    @Test
    void getUserProducts_success_returnsList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(userProductRepository.findByUser(user)).thenReturn(List.of(
                UserProduct.builder().userId(user.getId()).productId(product.getId()).product(product).build()));

        var result = userService.getUserProducts(request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(product.getId());
    }

    @Test
    void setGenresToUser_success_clearsAndAddsGenres() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);

        Boolean result = userService.setGenresToUser(request, List.of(Genre.ROCK, Genre.METAL));

        assertThat(result).isTrue();
        verify(userGenreRepository, times(1)).deleteAllByUser(user.getId());
        verify(userGenreRepository, times(1)).saveByUserIdAndGenre(user.getId(), Genre.ROCK.getCapsValue());
        verify(userGenreRepository, times(1)).saveByUserIdAndGenre(user.getId(), Genre.METAL.getCapsValue());
    }

    @Test
    void setTypesOfMusiciansToUser_success_clearsAndAddsTypes() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);

        Boolean result = userService.setTypesOfMusiciansToUser(request,
                List.of(TypeOfMusician.GUITARIST, TypeOfMusician.DRUMMER));

        assertThat(result).isTrue();
        verify(userTypeOfMusicianRepository, times(1)).deleteAllByUser(user.getId());
        verify(userTypeOfMusicianRepository, times(1)).saveByUserIdAndTypeOfMusician(
                user.getId(), TypeOfMusician.GUITARIST.getCapsValue());
        verify(userTypeOfMusicianRepository, times(1)).saveByUserIdAndTypeOfMusician(
                user.getId(), TypeOfMusician.DRUMMER.getCapsValue());
    }

    @Test
    void getGenresByUser_success_returnsList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(userGenreRepository.findByUser(user)).thenReturn(List.of(
                UserGenre.builder().userId(user.getId()).user(user).genre(Genre.ROCK).build()));

        var result = userService.getGenresByUser(request);

        assertThat(result).containsExactly(Genre.ROCK);
    }

    @Test
    void getTypesOfMusiciansByUser_success_returnsList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(userTypeOfMusicianRepository.findByUser(user)).thenReturn(List.of(
                UserTypeOfMusician.builder().userId(user.getId()).user(user).typeOfMusician(TypeOfMusician.DRUMMER)
                        .build()));

        var result = userService.getTypesOfMusiciansByUser(request);

        assertThat(result).containsExactly(TypeOfMusician.DRUMMER);
    }

    @Test
    void getUserInfoById_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.getUserInfoById(99L));
    }
}
