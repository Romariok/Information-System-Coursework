package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import lombok.Data;

@Data
public class MusicianProductId implements Serializable {
   private Long musicianId;
   private Long productId;
}
