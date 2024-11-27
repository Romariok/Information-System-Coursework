package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import lombok.Data;

@Data
public class UserProductId implements Serializable {
   private Long userId;
   private Long productId;
}
