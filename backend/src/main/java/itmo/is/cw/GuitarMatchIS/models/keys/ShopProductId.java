package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import lombok.Data;

@Data
public class ShopProductId implements Serializable {
   private Long shopId;
   private Long productId;
}
