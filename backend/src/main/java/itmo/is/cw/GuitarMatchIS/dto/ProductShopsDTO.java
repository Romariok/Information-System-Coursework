package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ProductShopsDTO {
   private ProductDTO product;
   private List<ProductShopDTO> shops;
}

