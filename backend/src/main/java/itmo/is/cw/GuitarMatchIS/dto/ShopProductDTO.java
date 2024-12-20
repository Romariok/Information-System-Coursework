package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ShopProductDTO {
   private Shop shop;
   private List<ProductOfShopDTO> products;
}
