package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import itmo.is.cw.GuitarMatchIS.models.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
   Page<Feedback> findByProductId(Long productId, Pageable pageable);

   Page<Feedback> findByArticleId(Long articleId, Pageable pageable);

   @Procedure(procedureName = "add_product_feedback")
   void addProductFeedback(
         @Param("p_user_id") Long userId,
         @Param("p_product_id") Long productId,
         @Param("p_text") String text,
         @Param("p_stars") int stars);

   @Procedure(procedureName = "add_article_feedback")
   void addArticleFeedback(
         @Param("p_user_id") Long userId,
         @Param("p_article_id") Long articleId,
         @Param("p_text") String text,
         @Param("p_stars") int stars);

}
