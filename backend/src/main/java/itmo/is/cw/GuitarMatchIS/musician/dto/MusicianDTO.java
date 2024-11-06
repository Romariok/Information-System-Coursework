package itmo.is.cw.GuitarMatchIS.musician.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class MusicianDTO {
   private Long id;
   private String name;
   private Integer subscribers;
}
