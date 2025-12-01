package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.models.Article;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
   boolean existsByHeader(String header);

   @Query("SELECT moderate_article(:articleId, :accepted, :moderatorId)")
   boolean moderateArticle(Long articleId, boolean accepted, Long moderatorId);

   @EntityGraph(attributePaths = "author")
   Page<Article> findByHeaderContainingAndAccepted(String header, boolean accepted, Pageable page);

   @EntityGraph(attributePaths = "author")
   Page<Article> findByAuthorIdAndAccepted(Long authorId, boolean accepted, Pageable page);

   @EntityGraph(attributePaths = "author")
   Page<Article> findByAccepted(boolean accepted, Pageable page);

   @EntityGraph(attributePaths = "author")
   List<Article> findByAccepted(boolean accepted);

}
