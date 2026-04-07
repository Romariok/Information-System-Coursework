package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.models.ArticleSort;
import itmo.is.cw.GuitarMatchIS.models.MusicianSort;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import itmo.is.cw.GuitarMatchIS.service.BrandService;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Section F — Пагинация граничные значения (6.26–6.30).
 */
@ExtendWith(MockitoExtension.class)
class PaginationEdgeCasesTest {

    // ---- ArticleService ----
    @Mock private ArticleRepository articleRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductArticleRepository productArticleRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @InjectMocks private ArticleService articleService;

    // ---- MusicianService ----
    @Mock private MusicianRepository musicianRepository;
    @Mock private MusicianGenreRepository musicianGenreRepository;
    @Mock private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
    @Mock private MusicianProductRepository musicianProductRepository;
    @InjectMocks private MusicianService musicianService;

    // ---- BrandService ----
    @Mock private BrandRepository brandRepository;
    @InjectMocks private BrandService brandService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/nonexistent-parser");
        ReflectionTestUtils.setField(articleService, "articlesTopic", "/articles");
    }

    /**
     * 6.26: size=0 в getMusician → IllegalArgumentException → 400.
     */
    @Test
    void getMusician_sizeZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> musicianService.getMusician(0, 0, MusicianSort.NAME, true));
    }

    /**
     * 6.26 (дополнительно): size=0 в getAcceptedArticles → IllegalArgumentException.
     */
    @Test
    void getAcceptedArticles_sizeZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> articleService.getAcceptedArticles(0, 0, ArticleSort.CREATED_AT, true));
    }

    /**
     * 6.27: from=-1 → Pagification.createPageTemplate выбрасывает ValidationException → 400.
     * (Методы, использующие Pagification: getArticlesByHeaderContaining, getBrands и др.)
     */
    @Test
    void getArticlesByHeaderContaining_negativeFrom_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> articleService.getArticlesByHeaderContaining("test", -1, 10));
    }

    /**
     * 6.27 (дополнительно): getBrands с from=-1 → ValidationException.
     */
    @Test
    void getBrands_negativeFrom_throwsValidationException() {
        assertThrows(ValidationException.class,
                () -> brandService.getBrands(-1, 10));
    }

    /**
     * 6.28: from=1000000 → репозиторий возвращает пустую страницу → список пустой, 200.
     */
    @Test
    void getMusician_veryLargeFrom_returnsEmptyList() {
        when(musicianRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<?> result = musicianService.getMusician(1_000_000, 10, MusicianSort.NAME, true);

        assertThat(result).isEmpty();
    }

    /**
     * 6.29: size=-5 → IllegalArgumentException → 400.
     */
    @Test
    void getMusician_negativeSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> musicianService.getMusician(0, -5, MusicianSort.NAME, true));
    }

    /**
     * 6.30: size=Integer.MAX_VALUE → мок репозитория возвращает пустую страницу → 200.
     */
    @Test
    void getAcceptedArticles_maxIntSize_returnsEmptyList() {
        when(articleRepository.findByAccepted(eq(true), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<?> result = articleService.getAcceptedArticles(0, Integer.MAX_VALUE, ArticleSort.CREATED_AT, true);

        assertThat(result).isEmpty();
    }
}
