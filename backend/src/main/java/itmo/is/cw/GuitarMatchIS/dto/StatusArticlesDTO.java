package itmo.is.cw.GuitarMatchIS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
public class StatusArticlesDTO {
   private ArticleDTO article;
   private ProductDTO product;
}
