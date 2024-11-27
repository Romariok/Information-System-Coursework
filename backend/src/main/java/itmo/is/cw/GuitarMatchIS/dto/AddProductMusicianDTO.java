package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddProductMusicianDTO {
   @NotBlank
   private String musicianName;
   @NotNull
   @Positive
   private Long productId;
}
