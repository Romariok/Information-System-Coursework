package itmo.is.cw.GuitarMatchIS.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.repository.ArticleRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleAlreadyExustsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleService {
   private final ArticleRepository articleRepository;
   private final JwtUtils jwtUtils;
   private final UserRepository userRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<ArticleDTO> getArticles(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Article> articles = articleRepository.findAll(page).getContent();

      return articles
            .stream()
            .map(article -> ArticleDTO.builder()
                  .id(article.getId())
                  .header(article.getHeader())
                  .text(article.getText())
                  .author(article.getAuthor().getUsername())
                  .createdAt(article.getCreatedAt())
                  .accepted(article.getAccepted())
                  .build())
            .sorted(new Comparator<ArticleDTO>() {
               @Override
               public int compare(ArticleDTO o1, ArticleDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public List<ArticleDTO> getArticlesByHeaderContaining(String header, int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Article> articles = articleRepository.findByHeaderContaining(header, page).getContent();

      return articles
            .stream()
            .map(article -> ArticleDTO.builder()
                  .id(article.getId())
                  .header(article.getHeader())
                  .text(article.getText())
                  .author(article.getAuthor().getUsername())
                  .createdAt(article.getCreatedAt())
                  .accepted(article.getAccepted())
                  .build())
            .sorted(new Comparator<ArticleDTO>() {
               @Override
               public int compare(ArticleDTO o1, ArticleDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public List<ArticleDTO> getArticlesByAuthorId(Long authorId, int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Article> articles = articleRepository.findByAuthorId(authorId, page).getContent();

      return articles
            .stream()
            .map(article -> ArticleDTO.builder()
                  .id(article.getId())
                  .header(article.getHeader())
                  .text(article.getText())
                  .author(article.getAuthor().getUsername())
                  .createdAt(article.getCreatedAt())
                  .accepted(article.getAccepted())
                  .build())
            .sorted(new Comparator<ArticleDTO>() {
               @Override
               public int compare(ArticleDTO o1, ArticleDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public boolean moderateArticle(Long articleId, boolean accepted, HttpServletRequest request) {
      User moderator = findUserByRequest(request);
      if (!moderator.getIsAdmin())
            throw new ForbiddenException("User have no rights to moderate article");

      simpMessagingTemplate.convertAndSend("/articles", "Article was moderated");
      return articleRepository.moderateArticle(articleId, accepted, moderator.getId());
   }

   public ArticleDTO createArticle(CreateArticleDTO createArticleDTO, HttpServletRequest request) {
      if (articleRepository.existsByHeader(createArticleDTO.getHeader()))
         throw new ArticleAlreadyExustsException(String.format("Article with header %s already exists",
               createArticleDTO.getHeader()));

      User author = findUserByRequest(request);

      Article article = Article.builder()
            .header(createArticleDTO.getHeader())
            .text(createArticleDTO.getText())
            .author(author)
            .createdAt(LocalDateTime.now())
            .accepted(createArticleDTO.getAccepted())
            .build();
      articleRepository.save(article);
      simpMessagingTemplate.convertAndSend("/articles", "New Article created");

      return new ArticleDTO(article.getId(),
            article.getHeader(),
            article.getText(),
            article.getAuthor().getUsername(),
            article.getCreatedAt(),
            article.getAccepted());
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username).get();
   }
}