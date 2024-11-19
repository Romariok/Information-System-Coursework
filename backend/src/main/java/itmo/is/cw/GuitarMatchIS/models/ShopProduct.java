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
public class ShopProduct {
   @EmbeddedId
   private ShopProductId id;

   @ManyToOne
   @MapsId("shopId")
   @JoinColumn(name = "shop_id", foreignKey = @ForeignKey(name = "fk_shop_product_shop"))
   private Shop shop;

   @ManyToOne
   @MapsId("productId")
   @JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_shop_product_product"))
   private Product product;

   @Column(name = "price", nullable = false)
   private Double price;

   @Column(name = "available", nullable = false)
   private Boolean available;
}
