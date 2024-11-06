package itmo.is.cw.GuitarMatchIS.musician.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateMusicianDTO {
   @NotBlank
   private String name;
}
