package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.Data;

@Data
public class UserTypeOfMusicianId implements Serializable {
   private static final long serialVersionUID = 1L;
   private Long userId;
   private TypeOfMusician typeOfMusician;
}
