package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.ModerateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Infrastructure resilience tests covering:
 * - Section A: PostgreSQL unavailable (6.3, 6.4)
 * - Section D: WebSocket unavailable (6.15–6.19)
 * - Section G: Concurrent access race conditions (6.31, 6.32)
 */
@ExtendWith(MockitoExtension.class)
class InfrastructureFailureTest {

    // ---- Shared mocks (injected into both ArticleService and MusicianService) ----
    @Mock private JwtUtils jwtUtils;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @Mock private ProductRepository productRepository;

    // ---- ArticleService-specific mocks ----
    @Mock private ArticleRepository articleRepository;
    @Mock private ProductArticleRepository productArticleRepository;
    @InjectMocks private ArticleService articleService;

    // ---- MusicianService-specific mocks ----
    @Mock private MusicianRepository musicianRepository;
    @Mock private MusicianGenreRepository musicianGenreRepository;
    @Mock private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
    @Mock private UserMusicianRepository userMusicianRepository;
    @Mock private MusicianProductRepository musicianProductRepository;
    @InjectMocks private MusicianService musicianService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/nonexistent-parser");
        ReflectionTestUtils.setField(articleService, "articlesTopic", "/articles");
    }

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    // =========================================================================
    // Section A — PostgreSQL unavailable
    // =========================================================================

    /**
     * 6.3: articleRepository.save brosает DataIntegrityViolationException →
     * productArticleRepository.save НЕ вызывается.
     */
    @Test
    void articleSave_dataIntegrityViolation_productArticleSaveNotCalled() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        mockFindUserByRequest(request, author);
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson Les Paul")).thenReturn(TestDataFactory.buildProduct());
        when(articleRepository.existsByHeader("Test Article")).thenReturn(false);
        when(articleRepository.save(any())).thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThrows(DataIntegrityViolationException.class,
                () -> articleService.createArticle(
                        new CreateArticleDTO("Gibson Les Paul", "Test Article", "Content"), request));

        verify(productArticleRepository, never()).save(any());
    }

    /**
     * 6.4: musicianRepository.save бросает DataIntegrityViolationException →
     * musicianGenreRepository.saveByMusicianIdAndGenre и simpMessagingTemplate НЕ вызываются.
     */
    @Test
    void musicianSave_dataIntegrityViolation_genreSaveAndWebSocketNotCalled() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("New Musician")).thenReturn(false);
        when(musicianRepository.save(any())).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        assertThrows(DataIntegrityViolationException.class,
                () -> musicianService.createMusician(
                        new CreateMusicianDTO("New Musician", List.of(), List.of()), request));

        verify(musicianGenreRepository, never()).saveByMusicianIdAndGenre(any(), any());
        verify(simpMessagingTemplate, never()).convertAndSend(anyString(), (Object) any());
    }

    // =========================================================================
    // Section D — WebSocket unavailable
    // =========================================================================

    /**
     * 6.15: createArticle — simpMessagingTemplate.convertAndSend бросает MessageDeliveryException →
     * метод завершается успешно, ответ 200; статья сохранена в БД.
     */
    @Test
    void createArticle_webSocketDeliveryException_returnsSuccessfully() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        mockFindUserByRequest(request, author);
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson Les Paul")).thenReturn(TestDataFactory.buildProduct());
        when(articleRepository.existsByHeader("Test Article")).thenReturn(false);
        Article savedArticle = TestDataFactory.buildArticle();
        when(articleRepository.save(any())).thenReturn(savedArticle);
        doThrow(new MessageDeliveryException("WebSocket unavailable"))
                .when(simpMessagingTemplate).convertAndSend(anyString(), (Object) any());

        ArticleDTO result = articleService.createArticle(
                new CreateArticleDTO("Gibson Les Paul", "Test Article", "Content"), request);

        assertThat(result).isNotNull();
        verify(articleRepository, times(1)).save(any());
    }

    /**
     * 6.16: createMusician — WebSocket падает → музыкант сохранён, метод возвращает MusicianInfoDTO.
     */
    @Test
    void createMusician_webSocketDeliveryException_returnsSuccessfully() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("New Musician")).thenReturn(false);
        Musician savedMusician = TestDataFactory.buildMusician();
        when(musicianRepository.save(any())).thenReturn(savedMusician);
        doThrow(new MessageDeliveryException("WebSocket unavailable"))
                .when(simpMessagingTemplate).convertAndSend(anyString(), (Object) any());

        MusicianInfoDTO result = musicianService.createMusician(
                new CreateMusicianDTO("New Musician", List.of(), List.of()), request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Musician");
    }

    /**
     * 6.17: subscribeToMusician — WebSocket падает → подписка создана, возвращает true.
     */
    @Test
    void subscribeToMusician_webSocketDeliveryException_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(false);
        doThrow(new MessageDeliveryException("WebSocket unavailable"))
                .when(simpMessagingTemplate).convertAndSend(anyString(), (Object) any());

        Boolean result = musicianService.subscribeToMusician(new SubscribeDTO(1L), request);

        assertThat(result).isTrue();
    }

    /**
     * 6.18: moderateArticle — WebSocket падает → статья обработана в БД, метод возвращает true.
     */
    @Test
    void moderateArticle_webSocketDeliveryException_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User admin = TestDataFactory.buildAdmin();
        mockFindUserByRequest(request, admin);
        when(articleRepository.existsById(1L)).thenReturn(true);
        when(articleRepository.moderateArticle(anyLong(), any(Boolean.class), anyLong())).thenReturn(true);
        doThrow(new MessageDeliveryException("WebSocket unavailable"))
                .when(simpMessagingTemplate).convertAndSend(anyString(), (Object) any());

        ModerateArticleDTO dto = ModerateArticleDTO.builder().articleId(1L).accepted(true).build();
        boolean result = articleService.moderateArticle(dto, request);

        assertThat(result).isTrue();
    }

    /**
     * 6.19: createArticle — WebSocket работает нормально → convertAndSend вызывается ровно 1 раз.
     */
    @Test
    void createArticle_webSocketWorks_convertAndSendCalledOnce() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        mockFindUserByRequest(request, author);
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson Les Paul")).thenReturn(TestDataFactory.buildProduct());
        when(articleRepository.existsByHeader("Test Article")).thenReturn(false);
        Article savedArticle = TestDataFactory.buildArticle();
        when(articleRepository.save(any())).thenReturn(savedArticle);

        articleService.createArticle(
                new CreateArticleDTO("Gibson Les Paul", "Test Article", "Content"), request);

        verify(simpMessagingTemplate, times(1)).convertAndSend(anyString(), (Object) any());
    }

    // =========================================================================
    // Section G — Concurrent access (race conditions)
    // =========================================================================

    /**
     * 6.31: subscribeToMusician — гонка: userMusicianRepository.subscribeToMusician бросает
     * DataIntegrityViolationException (после исправления G7) → SubscriptionAlreadyExistsException.
     */
    @Test
    void subscribeToMusician_raceCondition_throwsSubscriptionAlreadyExistsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Musician musician = TestDataFactory.buildMusician();
        mockFindUserByRequest(request, user);
        when(musicianRepository.findById(1L)).thenReturn(Optional.of(musician));
        when(userMusicianRepository.existsByUserAndMusician(user, musician)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("Unique constraint violation"))
                .when(userMusicianRepository).subscribeToMusician(anyLong(), anyLong());

        assertThrows(SubscriptionAlreadyExistsException.class,
                () -> musicianService.subscribeToMusician(new SubscribeDTO(1L), request));
    }

    /**
     * 6.32: createMusician — гонка: оба потока прошли existsByName=false, затем save бросает
     * DataIntegrityViolationException. Gap G7b: исключение не оборачивается в MusicianAlreadyExistsException.
     */
    @Test
    void createMusician_constraintViolationAfterCheck_propagatesDataIntegrityViolationException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        mockFindUserByRequest(request, user);
        when(musicianRepository.existsByName("New Musician")).thenReturn(false);
        when(musicianRepository.save(any())).thenThrow(
                new DataIntegrityViolationException("Duplicate musician — race condition gap G7b"));

        assertThrows(DataIntegrityViolationException.class,
                () -> musicianService.createMusician(
                        new CreateMusicianDTO("New Musician", List.of(), List.of()), request));
    }
}
