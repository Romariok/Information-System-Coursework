package itmo.is.cw.GuitarMatchIS.models.keys;

import java.io.Serializable;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import lombok.Data;

@Data
public class UserGenreId implements Serializable{
   private Long userId;
   private Genre genre;
}
