package itmo.is.cw.GuitarMatchIS.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.*;
import itmo.is.cw.GuitarMatchIS.models.Feedback;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.FeedbackRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedbackService {
      private final FeedbackRepository feedbackRepository;
      private final JwtUtils jwtUtils;
      private final UserRepository userRepository;
      private final ProductRepository productRepository;
      private final ArticleRepository articleRepository;
      private final SimpMessagingTemplate simpMessagingTemplate;

      @Value("${app.messaging.feedback-topic:/feedbacks}")
      private String feedbackTopic;

      public List<FeedbackDTO> getFeedbackByProductId(Long productId, int from, int size) {
            log.info("Fetching feedback for product with id: {}", productId);
            Pageable pageable = Pagification.createPageTemplate(from, size);

            List<Feedback> feedbacks = feedbackRepository.findByProductId(productId, pageable).getContent();

            return feedbacks
                        .stream()
                        .map(this::convertToDTO)
                        .sorted(Comparator.comparing(FeedbackDTO::getId))
                        .toList();

      }

      public List<FeedbackDTO> getFeedbackByArticleId(Long articleId, int from, int size) {
            log.info("Fetching feedback for article with id: {}", articleId);
            Pageable pageable = Pagification.createPageTemplate(from, size);

            List<Feedback> feedbacks = feedbackRepository.findByArticleId(articleId, pageable).getContent();

            return feedbacks
                        .stream()
                        .map(this::convertToDTO)
                        .sorted(Comparator.comparing(FeedbackDTO::getId))
                        .toList();
      }

      @Transactional
      public Boolean addProductFeedback(CreateProductFeedbackDTO feedbackDTO, HttpServletRequest request) {
            log.info("Adding feedback for product with id: {}", feedbackDTO.getProductId());
            Product product = productRepository.findById(feedbackDTO.getProductId())
                        .orElseThrow(() -> {
                              log.warn("Product with id {} not found while adding feedback",
                                          feedbackDTO.getProductId());
                              return new ProductNotFoundException(
                                          String.format("Product with id %s not found", feedbackDTO.getProductId()));
                        });

            User user = findUserByRequest(request);
            log.info("Feedback author is {}", user.getUsername());
            feedbackRepository.addProductFeedback(user.getId(), product.getId(), feedbackDTO.getText(),
                        feedbackDTO.getStars());
            simpMessagingTemplate.convertAndSend(feedbackTopic, "New Feedback created for product");
            log.info("Feedback for product with id {} successfully added", feedbackDTO.getProductId());
            return true;
      }

      @Transactional
      public Boolean addArticleFeedback(CreateArticleFeedbackDTO feedbackDTO, HttpServletRequest request) {
            log.info("Adding feedback for article with id: {}", feedbackDTO.getArticleId());
            Article article = articleRepository.findById(feedbackDTO.getArticleId())
                        .orElseThrow(() -> {
                              log.warn("Article with id {} not found while adding feedback",
                                          feedbackDTO.getArticleId());
                              return new ArticleNotFoundException(
                                          String.format("Article with id %s not found", feedbackDTO.getArticleId()));
                        });

            User user = findUserByRequest(request);
            log.info("Feedback author is {}", user.getUsername());
            feedbackRepository.addArticleFeedback(user.getId(), article.getId(), feedbackDTO.getText(),
                        feedbackDTO.getStars());
            simpMessagingTemplate.convertAndSend(feedbackTopic, "New Feedback created for article");
            log.info("Feedback for article with id {} successfully added", feedbackDTO.getArticleId());
            return true;
      }

      private User findUserByRequest(HttpServletRequest request) {
            String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
            return userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                    String.format("Username %s not found", username)));
      }

      private FeedbackDTO convertToDTO(Feedback feedback) {
            return FeedbackDTO.builder()
                        .id(feedback.getId())
                        .author(UserInfoDTO.builder().id(feedback.getAuthor().getId())
                                    .username(feedback.getAuthor().getUsername()).build())
                        .product(feedback.getProduct() != null ? ProductDTO.builder()
                                    .id(feedback.getProduct().getId())
                                    .name(feedback.getProduct().getName())
                                    .description(feedback.getProduct().getDescription())
                                    .rate(feedback.getProduct().getRate())
                                    .brand(BrandDTO.builder()
                                                .id(feedback.getProduct().getBrand().getId())
                                                .name(feedback.getProduct().getBrand().getName())
                                                .country(feedback.getProduct().getBrand().getCountry())
                                                .website(feedback.getProduct().getBrand().getWebsite())
                                                .email(feedback.getProduct().getBrand().getEmail())
                                                .build())
                                    .guitarForm(feedback.getProduct().getGuitarForm())
                                    .typeOfProduct(feedback.getProduct().getTypeOfProduct())
                                    .lads(feedback.getProduct().getLads())
                                    .avgPrice(feedback.getProduct().getAvgPrice())
                                    .color(feedback.getProduct().getColor())
                                    .strings(feedback.getProduct().getStrings())
                                    .tipMaterial(feedback.getProduct().getTipMaterial())
                                    .bodyMaterial(feedback.getProduct().getBodyMaterial())
                                    .pickupConfiguration(feedback.getProduct().getPickupConfiguration())
                                    .typeComboAmplifier(feedback.getProduct().getTypeComboAmplifier())
                                    .build() : null)
                        .article(feedback.getArticle() != null ? ArticleDTO.builder()
                                    .id(feedback.getArticle().getId())
                                    .header(feedback.getArticle().getHeader())
                                    .text(feedback.getArticle().getText())
                                    .author(UserInfoDTO.builder().id(feedback.getArticle().getAuthor().getId())
                                                .username(feedback.getArticle().getAuthor().getUsername()).build())
                                    .createdAt(feedback.getArticle().getCreatedAt())
                                    .accepted(feedback.getArticle().getAccepted())
                                    .build() : null)
                        .createdAt(feedback.getCreatedAt())
                        .text(feedback.getText())
                        .stars(feedback.getStars())
                        .build();
      }

}
