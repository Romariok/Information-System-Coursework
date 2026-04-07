package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ModerateArticleDTO;
import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.ArticleSort;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.ProductArticle;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductArticleRepository productArticleRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        // @Value fields are not injected in pure Mockito unit tests.
        // Set a non-existent path so ProcessBuilder throws IOException,
        // triggering the fallback that returns the original markdownText.
        ReflectionTestUtils.setField(articleService, "markdownParserPath", "/nonexistent-parser");
        ReflectionTestUtils.setField(articleService, "articlesTopic", "/articles");
    }

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void getAcceptedArticles_returnsOnlyAccepted() {
        Article article = TestDataFactory.buildArticle();
        Page<Article> page = new PageImpl<>(List.of(article));
        when(articleRepository.findByAccepted(eq(true), any())).thenReturn(page);

        List<ArticleDTO> result = articleService.getAcceptedArticles(0, 10, ArticleSort.CREATED_AT, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(article.getId());
        verify(articleRepository, times(1)).findByAccepted(eq(true), any());
    }

    @Test
    void getAcceptedArticles_sizeZero_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> articleService.getAcceptedArticles(0, 0, ArticleSort.CREATED_AT, true));
    }

    @Test
    void createArticle_userNotFound_throwsUsernameNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn("unknownuser");
        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson Les Paul")).thenReturn(TestDataFactory.buildProduct());
        when(articleRepository.existsByHeader("Test Header")).thenReturn(false);
        when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> articleService.createArticle(
                        new CreateArticleDTO("Gibson Les Paul", "Test Header", "# Text"),
                        request));
    }

    @Test
    void moderateArticle_notAdmin_throwsForbiddenException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User regularUser = TestDataFactory.buildUser();
        mockFindUserByRequest(request, regularUser);

        ModerateArticleDTO dto = ModerateArticleDTO.builder()
                .articleId(1L)
                .accepted(true)
                .build();

        assertThrows(ForbiddenException.class,
                () -> articleService.moderateArticle(dto, request));
    }

    @Test
    void moderateArticle_articleNotFound_throwsArticleNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User admin = TestDataFactory.buildAdmin();
        mockFindUserByRequest(request, admin);
        when(articleRepository.existsById(99L)).thenReturn(false);

        ModerateArticleDTO dto = ModerateArticleDTO.builder()
                .articleId(99L)
                .accepted(true)
                .build();

        assertThrows(ArticleNotFoundException.class,
                () -> articleService.moderateArticle(dto, request));
    }

    @Test
    void getArticlesByHeaderContaining_emptyString_returnsEmptyList() {
        when(articleRepository.findByHeaderContainingAndAccepted(eq(""), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<ArticleDTO> result = articleService.getArticlesByHeaderContaining("", 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getArticlesByHeaderContaining_nonEmpty_returnsList() {
        Article article = TestDataFactory.buildArticle();
        when(articleRepository.findByHeaderContainingAndAccepted(eq("Test"), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(article)));

        List<ArticleDTO> result = articleService.getArticlesByHeaderContaining("Test", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(article.getId());
    }

    @Test
    void getStatusArticles_notAdmin_throwsForbiddenException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User regularUser = TestDataFactory.buildUser();
        mockFindUserByRequest(request, regularUser);

        assertThrows(ForbiddenException.class,
                () -> articleService.getStatusArticles(0, 10, request));
    }

    @Test
    void getStatusArticles_success_returnsList() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User admin = TestDataFactory.buildAdmin();
        mockFindUserByRequest(request, admin);
        Product product = TestDataFactory.buildProduct();
        Article article = TestDataFactory.buildPendingArticle();
        ProductArticle productArticle = new ProductArticle();
        productArticle.setProductId(product.getId());
        productArticle.setArticleId(article.getId());
        productArticle.setProduct(product);
        productArticle.setArticle(article);
        when(productArticleRepository.findByAccepted(eq(false), any()))
                .thenReturn(new PageImpl<>(List.of(productArticle)));

        var result = articleService.getStatusArticles(0, 10, request);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProduct().getId()).isEqualTo(product.getId());
    }

    @Test
    void createArticle_htmlContentPrecomputed_saveCalledWithNonNullHtmlContent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        mockFindUserByRequest(request, author);

        when(productRepository.existsByName("Gibson Les Paul")).thenReturn(true);
        when(productRepository.findByName("Gibson Les Paul")).thenReturn(TestDataFactory.buildProduct());
        when(articleRepository.existsByHeader("New Article Header")).thenReturn(false);

        Article savedArticle = Article.builder()
                .id(10L)
                .header("New Article Header")
                .text("# Some Markdown")
                .author(author)
                .accepted(false)
                .htmlContent("# Some Markdown")
                .build();
        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        when(productArticleRepository.save(any())).thenReturn(null);

        ArticleDTO result = articleService.createArticle(
                new CreateArticleDTO("Gibson Les Paul", "New Article Header", "# Some Markdown"),
                request);

        verify(articleRepository, times(1)).save(argThat(article ->
                article.getHtmlContent() != null));
        assertThat(result).isNotNull();
    }

    @Test
    void moderateArticle_success_acceptsArticle() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User admin = TestDataFactory.buildAdmin();
        mockFindUserByRequest(request, admin);
        when(articleRepository.existsById(1L)).thenReturn(true);
        when(articleRepository.moderateArticle(1L, true, admin.getId())).thenReturn(true);
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        ModerateArticleDTO dto = ModerateArticleDTO.builder()
                .articleId(1L)
                .accepted(true)
                .build();

        boolean result = articleService.moderateArticle(dto, request);

        assertThat(result).isTrue();
        verify(articleRepository, times(1)).moderateArticle(1L, true, admin.getId());
    }

    @Test
    void getArticlesByAuthorId_userNotFound_returnsEmptyList() {
        when(articleRepository.findByAuthorIdAndAccepted(eq(99L), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of()));

        List<ArticleDTO> result = articleService.getArticlesByAuthorId(99L, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void getArticlesByAuthorId_success_returnsList() {
        Article article = TestDataFactory.buildArticle();
        when(articleRepository.findByAuthorIdAndAccepted(eq(1L), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(article)));

        List<ArticleDTO> result = articleService.getArticlesByAuthorId(1L, 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(article.getId());
    }

    @Test
    void getArticleById_notFound_throwsArticleNotFoundException() {
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ArticleNotFoundException.class,
                () -> articleService.getArticleById(99L));
    }

    @Test
    void convertMarkdownToHtmlAsync_success_returnsHtml() throws Exception {
        Path script = Files.createTempFile("markdown-parser", ".sh");
        Files.writeString(script, "#!/bin/sh\necho \"<p>ok</p>\"\n", StandardCharsets.UTF_8);
        script.toFile().setExecutable(true);
        script.toFile().deleteOnExit();
        ReflectionTestUtils.setField(articleService, "markdownParserPath", script.toAbsolutePath().toString());

        String result = articleService.convertMarkdownToHtmlAsync("# Title")
                .get(5, TimeUnit.SECONDS);

        assertThat(result).contains("<p>ok</p>");
    }
}
