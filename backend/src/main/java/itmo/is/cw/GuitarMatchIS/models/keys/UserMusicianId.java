package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserMusicianId implements Serializable {
   private Long userId;
   private Long musicianId;
}
