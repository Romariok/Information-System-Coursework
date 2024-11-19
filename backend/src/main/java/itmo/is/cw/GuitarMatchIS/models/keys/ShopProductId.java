package itmo.is.cw.GuitarMatchIS.models.keys;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class ShopProductId {
   private Long shopId;
   private Long productId;
}
