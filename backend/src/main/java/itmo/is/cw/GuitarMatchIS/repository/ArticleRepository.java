package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import itmo.is.cw.GuitarMatchIS.models.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
   boolean existsByHeader(String header);

   @Query("SELECT moderate_article(:articleId, :accepted, :moderatorId)")
   boolean moderateArticle(Long articleId, boolean accepted, Long moderatorId);

   Page<Article> findByHeaderContaining(String header, Pageable page);

   Page<Article> findByAuthorId(Long authorId, Pageable page);
}
