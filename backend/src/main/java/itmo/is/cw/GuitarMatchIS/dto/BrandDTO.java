package itmo.is.cw.GuitarMatchIS.dto;

import itmo.is.cw.GuitarMatchIS.models.Country;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class BrandDTO {
   private Long id;
   private String name;
   private Country country;
   private String website;
   private String email;
}
