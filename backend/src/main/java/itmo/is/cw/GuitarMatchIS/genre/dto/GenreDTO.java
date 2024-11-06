package itmo.is.cw.GuitarMatchIS.genre.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class GenreDTO {
   private Long id;
   private String name;
}
