package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateMusicianDTO {
   @NotBlank
   private String name;
   private List<Genre> genres;
   private List<TypeOfMusician> typesOfMusician;
}
