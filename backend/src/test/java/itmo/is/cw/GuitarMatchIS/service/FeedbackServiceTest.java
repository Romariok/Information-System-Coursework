package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.CreateArticleFeedbackDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateProductFeedbackDTO;
import itmo.is.cw.GuitarMatchIS.dto.FeedbackDTO;
import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.Feedback;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.FeedbackRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(feedbackService, "feedbackTopic", "/feedbacks");
    }

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void getFeedbackByProductId_noFeedback_returnsEmptyList() {
        when(feedbackRepository.findByProductId(eq(1L), any())).thenReturn(new PageImpl<>(List.of()));

        List<FeedbackDTO> result = feedbackService.getFeedbackByProductId(1L, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void createProductFeedback_productNotFound_throwsProductNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> feedbackService.addProductFeedback(
                        new CreateProductFeedbackDTO(99L, "Great guitar!", 5), request));
    }

    @Test
    void createArticleFeedback_articleNotFound_throwsArticleNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(articleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ArticleNotFoundException.class,
                () -> feedbackService.addArticleFeedback(
                        new CreateArticleFeedbackDTO(99L, "Great article!", 5), request));
    }

    @Test
    void createProductFeedback_starsOutOfRange_servicePassesThroughWithoutValidating() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        mockFindUserByRequest(request, user);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(feedbackRepository).addProductFeedback(anyLong(), anyLong(), anyString(), anyInt());
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        // stars=6 is beyond the DTO constraint, but the service itself does not validate stars
        Boolean result = feedbackService.addProductFeedback(
                new CreateProductFeedbackDTO(1L, "Too many stars", 6), request);

        assertThat(result).isTrue();
        verify(feedbackRepository, times(1)).addProductFeedback(
                eq(user.getId()), eq(product.getId()), eq("Too many stars"), eq(6));
    }

    @Test
    void createArticleFeedback_starsOutOfRange_servicePassesThroughWithoutValidating() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Article article = TestDataFactory.buildArticle();
        mockFindUserByRequest(request, user);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        doNothing().when(feedbackRepository).addArticleFeedback(anyLong(), anyLong(), anyString(), anyInt());
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        Boolean result = feedbackService.addArticleFeedback(
                new CreateArticleFeedbackDTO(1L, "Too many stars", 6), request);

        assertThat(result).isTrue();
        verify(feedbackRepository, times(1)).addArticleFeedback(
                eq(user.getId()), eq(article.getId()), eq("Too many stars"), eq(6));
    }

    @Test
    void createProductFeedback_userNotFound_throwsUsernameNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn("missing");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());
        when(productRepository.findById(1L)).thenReturn(Optional.of(TestDataFactory.buildProduct()));

        assertThrows(UsernameNotFoundException.class,
                () -> feedbackService.addProductFeedback(
                        new CreateProductFeedbackDTO(1L, "Text", 5), request));
    }

    @Test
    void getFeedbackByArticleId_noFeedback_returnsEmptyList() {
        when(feedbackRepository.findByArticleId(eq(1L), any())).thenReturn(new PageImpl<>(List.of()));

        List<FeedbackDTO> result = feedbackService.getFeedbackByArticleId(1L, 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void createArticleFeedback_success_savesFeedback() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User user = TestDataFactory.buildUser();
        Article article = TestDataFactory.buildArticle();
        mockFindUserByRequest(request, user);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        doNothing().when(feedbackRepository).addArticleFeedback(anyLong(), anyLong(), anyString(), anyInt());
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        Boolean result = feedbackService.addArticleFeedback(
                new CreateArticleFeedbackDTO(1L, "Great article", 5), request);

        assertThat(result).isTrue();
        verify(feedbackRepository, times(1)).addArticleFeedback(
                eq(user.getId()), eq(article.getId()), eq("Great article"), eq(5));
    }

    @Test
    void convertToDTO_feedbackFields_areMapped() {
        User user = TestDataFactory.buildUser();
        Product product = TestDataFactory.buildProduct();
        Article article = TestDataFactory.buildArticle();
        Feedback feedback = Feedback.builder()
                .id(5L)
                .author(user)
                .product(product)
                .article(article)
                .createdAt(LocalDateTime.of(2024, 2, 1, 0, 0))
                .text("Solid review")
                .stars(4)
                .build();
        when(feedbackRepository.findByProductId(eq(1L), any())).thenReturn(new PageImpl<>(List.of(feedback)));

        List<FeedbackDTO> result = feedbackService.getFeedbackByProductId(1L, 0, 10);

        assertThat(result).hasSize(1);
        FeedbackDTO dto = result.get(0);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getAuthor().getUsername()).isEqualTo(user.getUsername());
        assertThat(dto.getProduct().getId()).isEqualTo(product.getId());
        assertThat(dto.getProduct().getBrand().getName()).isEqualTo(product.getBrand().getName());
        assertThat(dto.getArticle().getId()).isEqualTo(article.getId());
        assertThat(dto.getArticle().getHeader()).isEqualTo(article.getHeader());
        assertThat(dto.getText()).isEqualTo("Solid review");
        assertThat(dto.getStars()).isEqualTo(4);
    }
}
