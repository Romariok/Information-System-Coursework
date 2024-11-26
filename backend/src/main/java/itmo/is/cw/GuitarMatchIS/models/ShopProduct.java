package itmo.is.cw.GuitarMatchIS.models;

import itmo.is.cw.GuitarMatchIS.models.keys.ShopProductId;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "shop_product")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@IdClass(ShopProductId.class)
public class ShopProduct {
   @Id
   @Column(name = "shop_id")
   private Long shopId;

   @Id
   @Column(name = "product_id")
   private Long productId;
   
   @ManyToOne
   @JoinColumn(name = "shop_id", foreignKey = @ForeignKey(name = "fk_shop_product_shop"))
   private Shop shop;
   
   @ManyToOne
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_shop_product_product"))
   private Product product;

   @Column(name = "price", nullable = false)
   private Double price;

   @Column(name = "available", nullable = false)
   private Boolean available;
}
