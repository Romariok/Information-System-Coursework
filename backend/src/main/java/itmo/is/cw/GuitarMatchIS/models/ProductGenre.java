package itmo.is.cw.GuitarMatchIS.models;

import org.hibernate.annotations.ColumnTransformer;

import itmo.is.cw.GuitarMatchIS.models.keys.ProductGenreId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@IdClass(ProductGenreId.class)
public class ProductGenre {
   @Id
   @Column(name = "product_id")
   private Long productId;

   @ManyToOne
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_genre_product"))
   private Product product;

   @Id
   @Column(name = "genre")
   @Enumerated(EnumType.STRING)
   @ColumnTransformer(write = "CAST(? AS genre_enum)", read = "CAST(genre AS VARCHAR)")
   private Genre genre;
}
