package itmo.is.cw.GuitarMatchIS.dto;


import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class MusicianInfoDTO {
   private Long id;
   private String name;
   private Integer subscribers;
   private List<Genre> genres;
   private List<TypeOfMusician> typesOfMusicians;
   private List<ProductDTO> products;
}
