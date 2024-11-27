package itmo.is.cw.GuitarMatchIS.dto;

import java.util.List;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductArticleDTO {
   private ProductDTO product;
   private List<ArticleDTO> articles;
}
