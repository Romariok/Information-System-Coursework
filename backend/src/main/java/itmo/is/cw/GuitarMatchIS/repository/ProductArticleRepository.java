package itmo.is.cw.GuitarMatchIS.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import itmo.is.cw.GuitarMatchIS.models.Article;
import itmo.is.cw.GuitarMatchIS.models.Product;
import itmo.is.cw.GuitarMatchIS.models.ProductArticle;
import itmo.is.cw.GuitarMatchIS.models.keys.ProductArticleId;

public interface ProductArticleRepository extends JpaRepository<ProductArticle, ProductArticleId> {
   boolean existsByProductAndArticle(Product product, Article article);

   Page<ProductArticle> findByProduct(Product product, Pageable pageable);

   @Query("SELECT pa FROM ProductArticle pa WHERE pa.article.accepted = :accepted")
   Page<ProductArticle> findByAccepted(@Param("accepted") boolean accepted, Pageable pageable);

   @Query("SELECT pa FROM ProductArticle pa WHERE pa.product.id = :productId AND pa.article.accepted = :accepted")
   Page<ProductArticle> findByProductIdAndAccepted(@Param("productId") Long productId,
         @Param("accepted") boolean accepted, Pageable pageable);
}
