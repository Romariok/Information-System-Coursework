package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class UserTypeOfMusicianDTO {
   private List<TypeOfMusician> typesOfMusicians;
}
