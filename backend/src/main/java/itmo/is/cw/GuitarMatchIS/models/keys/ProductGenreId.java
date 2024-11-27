package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import lombok.Data;

@Data
public class ProductGenreId implements Serializable {
   private Long productId;
   private Genre genre;
}
