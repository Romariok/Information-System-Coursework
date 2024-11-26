package itmo.is.cw.GuitarMatchIS.dto;

import itmo.is.cw.GuitarMatchIS.models.Shop;
import itmo.is.cw.GuitarMatchIS.models.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ShopProductDTO {
   private Shop shop;
   private Product product;
   private Double price;
   private Boolean available;
}
