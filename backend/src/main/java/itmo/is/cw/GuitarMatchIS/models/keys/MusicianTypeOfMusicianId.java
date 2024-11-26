package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.Data;

@Data
public class MusicianTypeOfMusicianId implements Serializable {
   private Long musicianId;
   private TypeOfMusician typeOfMusician;
}
