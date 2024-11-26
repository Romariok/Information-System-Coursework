package itmo.is.cw.GuitarMatchIS.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.*;
import itmo.is.cw.GuitarMatchIS.models.Feedback;
import itmo.is.cw.GuitarMatchIS.models.Musician;
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
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
public class FeedbackService {
   private final FeedbackRepository feedbackRepository;
   private final JwtUtils jwtUtils;
   private final UserRepository userRepository;
   private final ProductRepository productRepository;
   private final ArticleRepository articleRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<FeedbackDTO> getFeedbackByProductId(Long productId, int from, int size) {
      Pageable pageable = Pagification.createPageTemplate(from, size);

      List<Feedback> feedbacks = feedbackRepository.findByProductId(productId, pageable).getContent();

      return feedbacks
            .stream()
            .map(this::convertToDTO)
            .sorted(new Comparator<FeedbackDTO>() {
               @Override
               public int compare(FeedbackDTO o1, FeedbackDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();

   }

   public List<FeedbackDTO> getFeedbackByArticleId(Long articleId, int from, int size) {
      Pageable pageable = Pagification.createPageTemplate(from, size);

      List<Feedback> feedbacks = feedbackRepository.findByArticleId(articleId, pageable).getContent();

      return feedbacks
            .stream()
            .map(this::convertToDTO)
            .sorted(new Comparator<FeedbackDTO>() {
               @Override
               public int compare(FeedbackDTO o1, FeedbackDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   @Transactional
   public Boolean addProductFeedback(CreateProductFeedbackDTO feedbackDTO, HttpServletRequest request) {
      Product product = productRepository.findById(feedbackDTO.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(
                  String.format("Product with id %s not found", feedbackDTO.getProductId())));
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("User not found");
      }
      User user = findUserByRequest(request);
      feedbackRepository.addProductFeedback(user.getId(), product.getId(), feedbackDTO.getText(),
            feedbackDTO.getStars());
      simpMessagingTemplate.convertAndSend("/feedbacks", "New Feedback created for product");
      return true;
   }

   @Transactional
   public Boolean addArticleFeedback(CreateArticleFeedbackDTO feedbackDTO, HttpServletRequest request) {
      Article article = articleRepository.findById(feedbackDTO.getArticleId())
            .orElseThrow(() -> new ArticleNotFoundException(
                  String.format("Product with id %s not found", feedbackDTO.getArticleId())));
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("User not found");
      }
      User user = findUserByRequest(request);
      feedbackRepository.addArticleFeedback(user.getId(), article.getId(), feedbackDTO.getText(),
            feedbackDTO.getStars());
      simpMessagingTemplate.convertAndSend("/feedbacks", "New Feedback created for article");
      return true;
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username).get();
   }

   private FeedbackDTO convertToDTO(Feedback feedback) {
      return FeedbackDTO.builder()
            .id(feedback.getId())
            .authorId(feedback.getAuthor().getId())
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
                  .author(feedback.getArticle().getAuthor().getUsername())
                  .createdAt(feedback.getArticle().getCreatedAt())
                  .accepted(feedback.getArticle().getAccepted())
                  .build() : null)
            .createdAt(feedback.getCreatedAt())
            .text(feedback.getText())
            .stars(feedback.getStars())
            .build();
   }

}
