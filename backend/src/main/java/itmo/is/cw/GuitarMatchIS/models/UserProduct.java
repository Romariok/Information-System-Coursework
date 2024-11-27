package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.UserProductId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@Table(name = "product_user")
@IdClass(UserProductId.class)
public class UserProduct {
   @Id   
   @Column(name = "user_id")
   private Long userId;

   @Id
   @Column(name = "product_id")
   private Long productId;
   
   @ManyToOne
   @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_product_user_user"), updatable = false, insertable = false)
   private User user;

   @ManyToOne
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_user_product"), updatable = false, insertable = false)
   private Product product;
}
