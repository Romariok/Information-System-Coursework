package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.ProductArticleId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_articles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(ProductArticleId.class)
public class ProductArticle {
   @Id   
   @Column(name = "article_id")
   private Long articleId;

   @Id
   @Column(name = "product_id")
   private Long productId;
   
   @ManyToOne
   @JoinColumn(name = "article_id", foreignKey = @ForeignKey(name = "fk_product_articles_article"), updatable = false, insertable = false)
   private Article article;

   @ManyToOne
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_articles_product"), updatable = false, insertable = false)
   private Product product;

}
