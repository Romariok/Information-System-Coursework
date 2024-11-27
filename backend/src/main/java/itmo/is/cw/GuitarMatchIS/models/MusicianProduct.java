package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.MusicianProductId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@IdClass(MusicianProductId.class)
public class MusicianProduct {
   @Id   
   @Column(name = "musician_id")
   private Long musicianId;

   @Id
   @Column(name = "product_id")
   private Long productId;
   
   @ManyToOne
   @JoinColumn(name = "musician_id", foreignKey = @ForeignKey(name = "fk_musician_product_musician"), updatable = false, insertable = false)
   private Musician musician;

   @ManyToOne
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_musician_product_product"), updatable = false, insertable = false)
   private Product product;
}
