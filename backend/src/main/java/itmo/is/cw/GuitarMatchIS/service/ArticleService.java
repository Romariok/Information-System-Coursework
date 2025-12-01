package itmo.is.cw.GuitarMatchIS.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.ArticleSort;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.ProductArticle;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ModerateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.StatusArticlesDTO;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.ProductRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
   private final ArticleRepository articleRepository;
   private final JwtUtils jwtUtils;
   private final UserRepository userRepository;
   private final ProductRepository productRepository;
   private final ProductArticleRepository productArticleRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   @Value("${app.messaging.articles-topic:/articles}")
   private String articlesTopic;

   @Value("${app.external.markdown-parser.path:${user.dir}/parser}")
   private String markdownParserPath;

   @Cacheable(value = "articles", key = "'accepted:' + #from + ':' + #size + ':' + #sortBy + ':' + #ascending")
   public List<ArticleDTO> getAcceptedArticles(int from, int size, ArticleSort sortBy, boolean ascending) {
      log.info("Fetching accepted articles from: {}, size: {}, sortBy: {}, ascending: {}", from, size, sortBy,
            ascending);
      Sort sort = Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC,
            sortBy.getFieldName());
      Pageable page = PageRequest.of(from / size, size, sort);

      List<Article> articles = articleRepository.findByAccepted(true, page).getContent();

      return articles.stream()
            .map(this::convertToDTO)
            .toList();
   }

   public ArticleDTO getArticleById(Long id) {
      log.info("Fetching article by id: {}", id);
      return convertToDTO(articleRepository.findById(id)
            .orElseThrow(() -> {
               log.warn("Article with id {} not found", id);
               return new ArticleNotFoundException(String.format("Article with id %s not found", id));
            }));
   }

   public List<ArticleDTO> getArticlesByHeaderContaining(String header, int from, int size) {
      log.info("Fetching articles with header containing: {}", header);
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Article> articles = articleRepository.findByHeaderContainingAndAccepted(header, true, page).getContent();

      return articles
            .stream()
            .map(this::convertToDTO)
            .sorted(Comparator.comparing(ArticleDTO::getId))
            .toList();
   }

   public List<ArticleDTO> getArticlesByAuthorId(Long authorId, int from, int size) {
      log.info("Fetching articles by author id: {}", authorId);
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Article> articles = articleRepository.findByAuthorIdAndAccepted(authorId, true, page).getContent();

      return articles
            .stream()
            .map(this::convertToDTO)
            .sorted(Comparator.comparing(ArticleDTO::getId))
            .toList();
   }

   @CacheEvict(value = "articles", allEntries = true)
   public boolean moderateArticle(ModerateArticleDTO moderateArticleDTO, HttpServletRequest request) {
      User moderator = findUserByRequest(request);
      log.info("Moderating article with id: {} by user: {}", moderateArticleDTO.getArticleId(),
            moderator.getUsername());
      if (!moderator.getIsAdmin()) {
         log.warn("User {} has no rights to moderate article", moderator.getUsername());
         throw new ForbiddenException("User have no rights to moderate article");
      }
      Long articleId = moderateArticleDTO.getArticleId();
      if (!articleRepository.existsById(articleId)) {
         log.warn("Article with id {} not found for moderation", articleId);
         throw new ArticleNotFoundException(String.format("Article with id %s not found", articleId));
      }
      if (!moderateArticleDTO.isAccepted()) {
         // Delete the article if rejected
         log.info("Article with id {} was rejected and will be deleted", articleId);
         articleRepository.deleteById(articleId);
         simpMessagingTemplate.convertAndSend(articlesTopic, "Article was rejected and deleted");
         return true;
      }

      // Accept the article if not rejected
      log.info("Article with id {} was approved", articleId);
      simpMessagingTemplate.convertAndSend(articlesTopic, "Article was approved");
      return articleRepository.moderateArticle(articleId, true, moderator.getId());
   }

   @Transactional
   @CacheEvict(value = "articles", allEntries = true)
   public ArticleDTO createArticle(CreateArticleDTO createArticleDTO, HttpServletRequest request) {
      log.info("Creating article with header: {}", createArticleDTO.getHeader());
      if (!productRepository.existsByName(createArticleDTO.getProductName())) {
         log.warn("Product {} not found for article creation", createArticleDTO.getProductName());
         throw new ProductNotFoundException(String.format("Product %s not found", createArticleDTO.getProductName()));
      }
      Product product = productRepository.findByName(createArticleDTO.getProductName());

      if (articleRepository.existsByHeader(createArticleDTO.getHeader())) {
         log.warn("Article with header {} already exists", createArticleDTO.getHeader());
         throw new ArticleAlreadyExistsException(String.format("Article with header %s already exists",
               createArticleDTO.getHeader()));
      }
      User author = findUserByRequest(request);
      log.info("Article author is {}", author.getUsername());

      Article article = Article.builder()
            .header(createArticleDTO.getHeader())
            .text(createArticleDTO.getText())
            .author(author)
            .createdAt(LocalDateTime.now())
            .accepted(false)
            .build();
      articleRepository.save(article);
      log.info("Article with header {} saved with id {}", article.getHeader(), article.getId());

      ProductArticle productArticle = new ProductArticle();
      productArticle.setArticleId(article.getId());
      productArticle.setProductId(product.getId());
      productArticleRepository.save(productArticle);

      simpMessagingTemplate.convertAndSend("/articles", "New Article created for product " + product.getName());
      log.info("Article creation message sent to websocket");
      return convertToDTO(article);
   }

   public List<StatusArticlesDTO> getStatusArticles(int from, int size, HttpServletRequest request) {
      User user = findUserByRequest(request);
      log.info("User {} is requesting status articles", user.getUsername());
      if (!user.getIsAdmin()) {
         log.warn("User {} has no rights to get moderate articles", user.getUsername());
         throw new ForbiddenException("User have no rights to get moderate articles");
      }
      Pageable page = Pagification.createPageTemplate(from, size);

      List<ProductArticle> productArticles = productArticleRepository.findByAccepted(false, page).getContent();

      return productArticles
            .stream()
            .map(this::convertToDTO)
            .sorted(Comparator.comparing((StatusArticlesDTO dto) -> dto.getProduct().getId()))
            .toList();
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      log.debug("Finding user by username: {}", username);
      return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                  String.format("Username %s not found", username)));
   }

   private ArticleDTO convertToDTO(Article article) {
      String htmlContent = convertMarkdownToHtml(article.getText());

      return ArticleDTO.builder()
            .id(article.getId())
            .header(article.getHeader())
            .text(article.getText())
            .htmlContent(htmlContent)
            .author(UserInfoDTO.builder()
                  .id(article.getAuthor().getId())
                  .username(article.getAuthor().getUsername())
                  .build())
            .createdAt(article.getCreatedAt())
            .accepted(article.getAccepted())
            .build();
   }

   private StatusArticlesDTO convertToDTO(ProductArticle productArticle) {
      return StatusArticlesDTO.builder()
            .product(convertToDTO(productArticle.getProduct()))
            .article(convertToDTO(productArticle.getArticle()))
            .build();
   }

   private ProductDTO convertToDTO(Product product) {
      return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .rate(product.getRate())
            .brand(BrandDTO.builder()
                  .id(product.getBrand().getId())
                  .name(product.getBrand().getName())
                  .country(product.getBrand().getCountry())
                  .website(product.getBrand().getWebsite())
                  .email(product.getBrand().getEmail())
                  .build())
            .guitarForm(product.getGuitarForm())
            .typeOfProduct(product.getTypeOfProduct())
            .lads(product.getLads())
            .avgPrice(product.getAvgPrice())
            .color(product.getColor())
            .strings(product.getStrings())
            .tipMaterial(product.getTipMaterial())
            .bodyMaterial(product.getBodyMaterial())
            .pickupConfiguration(product.getPickupConfiguration())
            .typeComboAmplifier(product.getTypeComboAmplifier())
            .build();
   }

   private String convertMarkdownToHtml(String markdownText) {
      try {
         // Выполняем конвертацию в отдельной асинхронной задаче
         return convertMarkdownToHtmlAsync(markdownText).get();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         log.error("Markdown to HTML conversion was interrupted", e);
         return markdownText;
      } catch (ExecutionException e) {
         log.error("Exception while converting markdown to html", e.getCause());
         return markdownText;
      }
   }

   /**
    * Асинхронный вызов внешнего markdown-парсера.
    * Этот метод будет выполняться в отдельном потоке thread pool'а Spring.
    */
   @Async
   public CompletableFuture<String> convertMarkdownToHtmlAsync(String markdownText) {
      try {
         // Get parser path
         String parserPath = markdownParserPath;
         // Build the command - passing text directly
         log.debug("Converting markdown to html using parser at {}", parserPath);
         ProcessBuilder processBuilder = new ProcessBuilder(
               parserPath, "-c", markdownText);
         processBuilder.redirectErrorStream(true);
         // Execute the command
         Process process = processBuilder.start();
         // Read the output
         StringBuilder output = new StringBuilder();
         try (BufferedReader reader = new BufferedReader(
               new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
               output.append(line).append("\n");
            }
         }
         // Wait for process to complete
         int exitCode = process.waitFor();

         if (exitCode != 0) {
            log.error("Markdown parser failed with exit code {} and output: {}", exitCode, output.toString());
            return CompletableFuture.completedFuture(markdownText); // Fallback to original text
         }
         log.debug("Markdown successfully converted to HTML");
         return CompletableFuture.completedFuture(output.toString());
      } catch (IOException e) {
         log.error("IO error while converting markdown to html", e);
         return CompletableFuture.completedFuture(markdownText); // Fallback to original text
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         log.error("Markdown to HTML conversion was interrupted", e);
         return CompletableFuture.completedFuture(markdownText); // Fallback to original text
      }
   }
}
