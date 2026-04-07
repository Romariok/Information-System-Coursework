package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Section C — Haskell-парсер недоступен (6.10–6.13).
 *
 * В unit-тестах @Async не обрабатывается через AOP, поэтому
 * convertMarkdownToHtmlAsync запускается синхронно и возвращает CompletableFuture,
 * уже завершённый к моменту get().
 */
@ExtendWith(MockitoExtension.class)
class MarkdownParserResilienceTest {

    @Mock private ArticleRepository articleRepository;
    @Mock private JwtUtils jwtUtils;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductArticleRepository productArticleRepository;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private ArticleService articleService;

    private static final String MARKDOWN = "# Hello World\n\nSome **bold** text.";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(articleService, "articlesTopic", "/articles");
    }

    /**
     * 6.10: Парсер не найден (IOException при запуске процесса) →
     * htmlContent = markdownText (не пустая строка, не null).
     */
    @Test
    void parserNotFound_ioException_returnsFallbackToOriginalMarkdown() throws Exception {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/nonexistent-parser-path-xyz");

        CompletableFuture<String> result = articleService.convertMarkdownToHtmlAsync(MARKDOWN);

        assertThat(result.get()).isEqualTo(MARKDOWN);
    }

    /**
     * 6.11: Парсер завершается с exitCode != 0 →
     * htmlContent = markdownText (fallback).
     * Использует /usr/bin/false (всегда выходит с кодом 1, игнорируя аргументы).
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void parserExitCodeNonZero_returnsFallbackToOriginalMarkdown() throws Exception {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/usr/bin/false");

        CompletableFuture<String> result = articleService.convertMarkdownToHtmlAsync(MARKDOWN);

        assertThat(result.get()).isEqualTo(MARKDOWN);
    }

    /**
     * 6.12: Парсер завершается с exitCode=0 но возвращает пустой output →
     * после исправления G2: htmlContent = markdownText, не "".
     * Использует /bin/true (всегда выходит с кодом 0, ничего не печатает).
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    void parserExitZeroEmptyOutput_returnsFallbackToOriginalMarkdown() throws Exception {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/bin/true");

        CompletableFuture<String> result = articleService.convertMarkdownToHtmlAsync(MARKDOWN);

        String html = result.get();
        assertThat(html).isNotBlank();
        assertThat(html).isEqualTo(MARKDOWN);
    }

    /**
     * 6.14 (частичный): Парсер вызывается с непустым markdown →
     * если parsePath корректен, fallback не должен срабатывать;
     * если parsePath некорректен (unit-тест), возвращается исходный текст.
     * Этот тест документирует ожидаемое поведение: htmlContent не null и не пустой.
     */
    @Test
    void convertMarkdownToHtmlAsync_anyPath_returnsNonBlankString() throws Exception {
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/nonexistent-parser-path-xyz");

        CompletableFuture<String> result = articleService.convertMarkdownToHtmlAsync(MARKDOWN);

        assertThat(result.get()).isNotBlank();
    }
}
