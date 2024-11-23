package itmo.is.cw.GuitarMatchIS.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import itmo.is.cw.GuitarMatchIS.models.Feedback;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
   List<Feedback> findByProductId(Long productId);
   List<Feedback> findByArticleId(Long articleId);
   
   @Query("SELECT add_product_feedback(:userId, :productId, :text, :stars)")
   boolean addProductFeedback(Long userId, Long productId, String text, int stars);

   @Query("SELECT add_article_feedback(:userId, :articleId, :text, :stars)")
   boolean addArticleFeedback(Long userId, Long articleId, String text, int stars);

}
