package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class MusicianTypeOfMusicianDTO {
   private Musician musician;
   private List<TypeOfMusician> typeOfMusicians;
}
