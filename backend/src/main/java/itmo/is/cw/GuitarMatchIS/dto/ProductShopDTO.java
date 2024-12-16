package itmo.is.cw.GuitarMatchIS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ProductShopDTO {
   private Long id;
   private String name;
   private Double price;
   private Boolean available;
}
