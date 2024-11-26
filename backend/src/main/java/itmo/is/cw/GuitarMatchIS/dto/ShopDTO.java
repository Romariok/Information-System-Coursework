package itmo.is.cw.GuitarMatchIS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ShopDTO {
   private Long id;
   private String name;
   private String description;
   private String website;
   private String email;
   private String address;
}
