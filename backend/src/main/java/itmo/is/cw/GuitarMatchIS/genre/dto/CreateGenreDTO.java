package itmo.is.cw.GuitarMatchIS.genre.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateGenreDTO {
   @NotBlank
   private String name;
}
