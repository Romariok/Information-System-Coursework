package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import lombok.Data;

@Data
public class ShopProductId implements Serializable {
   private static final long serialVersionUID = 1L;
   private Long shopId;
   private Long productId;
}
