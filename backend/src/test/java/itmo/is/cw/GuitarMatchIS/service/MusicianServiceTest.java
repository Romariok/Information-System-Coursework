package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.AddProductMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.MusicianGenre;
import itmo.is.cw.GuitarMatchIS.models.MusicianSort;
import itmo.is.cw.GuitarMatchIS.models.MusicianTypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.*;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.*;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicianServiceTest {

    @Mock
    private MusicianRepository musicianRepository;

    @Mock
    private MusicianGenreRepository musicianGenreRepository;

    @Mock
    private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserMusicianRepository userMusicianRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MusicianProductRepository musicianProductRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private MusicianService musicianService;

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void getMusicianInfo_notFound_throwsMusicianNotFoundException() {
        when(musicianRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(MusicianNotFoundException.class,
                () -> musicianService.getMusicianInfo(99L));
    }

    @Test
    void createMusician_duplicate_throwsMusicianAlreadyExistsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);

        assertThrows(MusicianAlreadyExistsException.class,
                () -> musicianService.createMusician(
                        new CreateMusicianDTO("Test Musician", List.of(), List.of()),
                        request));
    }

    @Test
    void createMusician_unauthorized_throwsUsernameNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn("unknownuser");
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> musicianService.createMusician(
                        new CreateMusicianDTO("New Musician", List.of(), List.of()),
                        request));
    }

    @Test
    void subscribeToMusician_alreadySubscribed_throwsSubscriptionAlreadyExistsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(true);

        assertThrows(SubscriptionAlreadyExistsException.class,
                () -> musicianService.subscribeToMusician(new SubscribeDTO(1L), request));
    }

    @Test
    void unsubscribeFromMusician_noSubscription_throwsSubscriptionNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(false);

        assertThrows(SubscriptionNotFoundException.class,
                () -> musicianService.unsubscribeFromMusician(new SubscribeDTO(1L), request));
    }

    @Test
    void addProductToMusician_alreadyLinked_throwsProductMusicianAlreadyExists() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(musicianRepository.findByName("Test Musician")).thenReturn(musician);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(musicianProductRepository.existsByMusicianAndProduct(musician, product)).thenReturn(true);

        assertThrows(ProductMusicianAlreadyExists.class,
                () -> musicianService.addProductToMusician(
                        new AddProductMusicianDTO("Test Musician", 1L), request));
    }

    @Test
    void deleteProductFromMusician_notFound_throwsProductMusicianNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(musicianRepository.findByName("Test Musician")).thenReturn(musician);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(musicianProductRepository.existsByMusicianAndProduct(musician, product)).thenReturn(false);

        assertThrows(ProductMusicianNotFoundException.class,
                () -> musicianService.deleteProductFromMusician(
                        new AddProductMusicianDTO("Test Musician", 1L), request));
    }

    @Test
    void searchMusicians_emptyName_returnsEmptyList() {
        when(musicianRepository.findAllByNameContains(eq(""), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<MusicianInfoDTO> result = musicianService.searchMusicians("", 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getMusician_callsGenreRepoForEachMusician() {
        Musician m1 = TestDataFactory.buildMusician();
        Musician m2 = Musician.builder().id(2L).name("Musician 2").subscribers(0).build();
        Musician m3 = Musician.builder().id(3L).name("Musician 3").subscribers(0).build();

        Page<Musician> musicianPage = new PageImpl<>(List.of(m1, m2, m3));
        when(musicianRepository.findAll(any(Pageable.class))).thenReturn(musicianPage);
        when(musicianGenreRepository.findByMusician(any(Musician.class))).thenReturn(List.of());
        when(musicianTypeOfMusicianRepository.findByMusician(any(Musician.class))).thenReturn(List.of());
        when(musicianProductRepository.findByMusician(any(Musician.class))).thenReturn(List.of());

        List<MusicianInfoDTO> result = musicianService.getMusician(0, 10, MusicianSort.NAME, true);

        assertThat(result).hasSize(3);
        verify(musicianGenreRepository, times(3)).findByMusician(any(Musician.class));
    }

    @Test
    void getMusician_sortByUnsupported_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> musicianService.getMusician(0, 10, null, true));
    }

    @Test
    void getMusicianInfo_subscribersCount_returnsZero() {
        Musician musician = TestDataFactory.buildMusician();
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(musicianGenreRepository.findByMusician(musician)).thenReturn(List.of());
        when(musicianTypeOfMusicianRepository.findByMusician(musician)).thenReturn(List.of());
        when(musicianProductRepository.findByMusician(musician)).thenReturn(List.of());

        MusicianInfoDTO result = musicianService.getMusicianInfo(1L);

        assertThat(result.getSubscribers()).isZero();
    }

    @Test
    void unsubscribeFromMusician_success_deletesSubscription() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(true);

        Boolean result = musicianService.unsubscribeFromMusician(new SubscribeDTO(1L), request);

        assertThat(result).isTrue();
        verify(userMusicianRepository, times(1)).deleteByUserAndMusician(user, musician);
    }

    @Test
    void addProductToMusician_productNotFound_throwsProductNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThrows(ProductNotFoundException.class,
                () -> musicianService.addProductToMusician(
                        new AddProductMusicianDTO("Test Musician", 99L), request));
    }

    @Test
    void addProductToMusician_success_savesLink() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(musicianRepository.findByName("Test Musician")).thenReturn(musician);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(musicianProductRepository.existsByMusicianAndProduct(musician, product)).thenReturn(false);

        Boolean result = musicianService.addProductToMusician(
                new AddProductMusicianDTO("Test Musician", 1L), request);

        assertThat(result).isTrue();
        verify(musicianProductRepository, times(1)).save(any());
    }

    @Test
    void deleteProductFromMusician_success_deletesLink() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("Test Musician")).thenReturn(true);
        when(productRepository.existsById(1L)).thenReturn(true);
        when(musicianRepository.findByName("Test Musician")).thenReturn(musician);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(musicianProductRepository.existsByMusicianAndProduct(musician, product)).thenReturn(true);

        Boolean result = musicianService.deleteProductFromMusician(
                new AddProductMusicianDTO("Test Musician", 1L), request);

        assertThat(result).isTrue();
        verify(musicianProductRepository, times(1)).deleteByMusicianAndProduct(musician, product);
    }

    @Test
    void getMusiciansByGenre_returnsList() {
        Musician musician = TestDataFactory.buildMusician();
        MusicianGenre genre = MusicianGenre.builder()
                .musicianId(musician.getId())
                .musician(musician)
                .genre(Genre.ROCK)
                .build();
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(musicianGenreRepository.findByMusician(musician)).thenReturn(List.of(genre));

        var result = musicianService.getMusiciansByGenre(1L);

        assertThat(result.getGenres()).containsExactly(Genre.ROCK);
    }

    @Test
    void getMusiciansByTypeOfMusician_returnsList() {
        Musician musician = TestDataFactory.buildMusician();
        MusicianTypeOfMusician typeOfMusician = MusicianTypeOfMusician.builder()
                .musicianId(musician.getId())
                .musician(musician)
                .typeOfMusician(TypeOfMusician.GUITARIST)
                .build();
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(musicianTypeOfMusicianRepository.findByMusician(musician)).thenReturn(List.of(typeOfMusician));

        var result = musicianService.getMusiciansByTypeOfMusician(1L);

        assertThat(result.getTypeOfMusicians()).containsExactly(TypeOfMusician.GUITARIST);
    }

    @Test
    void isSubscribed_true_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(true);

        Boolean result = musicianService.isSubscribed(1L, request);

        assertThat(result).isTrue();
    }

    @Test
    void isSubscribed_false_returnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(false);

        Boolean result = musicianService.isSubscribed(1L, request);

        assertThat(result).isFalse();
    }
}
