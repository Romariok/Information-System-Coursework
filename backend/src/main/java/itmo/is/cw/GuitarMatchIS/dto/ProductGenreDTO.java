package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import itmo.is.cw.GuitarMatchIS.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ProductGenreDTO {
   private ProductDTO product;
   private List<Genre> genres;
}
