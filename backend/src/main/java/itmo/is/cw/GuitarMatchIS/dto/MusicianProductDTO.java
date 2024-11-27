package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MusicianProductDTO {
   private MusicianDTO musician;
   private List<ProductDTO> products;
}
