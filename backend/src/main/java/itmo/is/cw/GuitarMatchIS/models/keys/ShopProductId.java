package itmo.is.cw.GuitarMatchIS.models.keys;

import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.models.Product;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Embeddable
public class ShopProductId{
   @ManyToOne
   @JoinColumn(name = "shop_id")
   private Shop shop;

   @ManyToOne
   @JoinColumn(name = "product_id")
   private Product product;
}
