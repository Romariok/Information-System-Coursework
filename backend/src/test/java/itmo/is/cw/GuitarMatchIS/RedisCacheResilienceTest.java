package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.config.RedisCacheConfig;
import itmo.is.cw.GuitarMatchIS.models.ArticleSort;
import itmo.is.cw.GuitarMatchIS.models.MusicianSort;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.BrandRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianGenreRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.MusicianTypeOfMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import itmo.is.cw.GuitarMatchIS.service.BrandService;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Section B — Redis недоступен (6.6–6.8).
 * Тестирует CacheErrorHandler из RedisCacheConfig и поведение сервисов при отсутствии кэша.
 */
@ExtendWith(MockitoExtension.class)
class RedisCacheResilienceTest {

    // ---- CacheErrorHandler tests ----

    private CacheErrorHandler cacheErrorHandler;
    private Cache mockCache;

    // ---- BrandService mocks ----
    @Mock private BrandRepository brandRepository;
    @InjectMocks private BrandService brandService;

    // ---- MusicianService mocks ----
    @Mock private MusicianRepository musicianRepository;
    @Mock private MusicianGenreRepository musicianGenreRepository;
    @Mock private MusicianTypeOfMusicianRepository musicianTypeOfMusicianRepository;
    @Mock private MusicianProductRepository musicianProductRepository;
    @Mock private UserRepository userRepository;
    @Mock private JwtUtils jwtUtils;
    @InjectMocks private MusicianService musicianService;

    // ---- ArticleService mocks ----
    @Mock private ArticleRepository articleRepository;
    @InjectMocks private ArticleService articleService;

    @BeforeEach
    void setUp() {
        RedisCacheConfig config = new RedisCacheConfig();
        cacheErrorHandler = config.errorHandler();
        mockCache = mock(Cache.class);

        org.springframework.test.util.ReflectionTestUtils.setField(
                articleService, "markdownParserPath", "/nonexistent-parser");
        org.springframework.test.util.ReflectionTestUtils.setField(
                articleService, "articlesTopic", "/articles");
    }

    // =========================================================================
    // CacheErrorHandler — не бросает исключения при ошибках Redis
    // =========================================================================

    /**
     * 6.6 (часть): CacheErrorHandler.handleCacheGetError при RedisConnectionFailureException
     * не бросает исключение → метод сервиса вызывается (cache fallback).
     */
    @Test
    void cacheErrorHandler_handleGetError_doesNotThrow() {
        assertDoesNotThrow(() -> cacheErrorHandler.handleCacheGetError(
                new RedisConnectionFailureException("Connection refused"), mockCache, "testKey"));
    }

    /**
     * CacheErrorHandler.handleCachePutError при RedisConnectionFailureException не бросает.
     */
    @Test
    void cacheErrorHandler_handlePutError_doesNotThrow() {
        assertDoesNotThrow(() -> cacheErrorHandler.handleCachePutError(
                new RedisConnectionFailureException("Connection refused"), mockCache, "testKey", "value"));
    }

    /**
     * CacheErrorHandler.handleCacheEvictError при RedisConnectionFailureException не бросает.
     */
    @Test
    void cacheErrorHandler_handleEvictError_doesNotThrow() {
        assertDoesNotThrow(() -> cacheErrorHandler.handleCacheEvictError(
                new RedisConnectionFailureException("Connection refused"), mockCache, "testKey"));
    }

    /**
     * CacheErrorHandler.handleCacheClearError при RedisConnectionFailureException не бросает.
     */
    @Test
    void cacheErrorHandler_handleClearError_doesNotThrow() {
        assertDoesNotThrow(() -> cacheErrorHandler.handleCacheClearError(
                new RedisConnectionFailureException("Connection refused"), mockCache));
    }

    // =========================================================================
    // 6.6: getMusician — репозиторий вызывается (cache fallback работает)
    // В unit-тестах кэш не активен (@Cacheable без Spring-контекста), поэтому
    // каждый вызов идёт в репозиторий — имитирует поведение при Redis fallback.
    // =========================================================================

    @Test
    void getMusician_repositoryAlwaysCalled_cacheBypassBehavior() {
        when(musicianRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        musicianService.getMusician(0, 10, MusicianSort.NAME, true);

        verify(musicianRepository, times(1))
                .findAll(any(org.springframework.data.domain.Pageable.class));
    }

    // =========================================================================
    // 6.7: getBrands — репозиторий вызывается
    // =========================================================================

    @Test
    void getBrands_repositoryAlwaysCalled_cacheBypassBehavior() {
        when(brandRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        brandService.getBrands(0, 10);

        verify(brandRepository, times(1))
                .findAll(any(org.springframework.data.domain.Pageable.class));
    }

    // =========================================================================
    // 6.8: getAcceptedArticles — репозиторий вызывается
    // =========================================================================

    @Test
    void getAcceptedArticles_repositoryAlwaysCalled_cacheBypassBehavior() {
        when(articleRepository.findByAccepted(eq(true), any()))
                .thenReturn(new PageImpl<>(List.of()));

        articleService.getAcceptedArticles(0, 10, ArticleSort.CREATED_AT, true);

        verify(articleRepository, times(1)).findByAccepted(eq(true), any());
    }
}
