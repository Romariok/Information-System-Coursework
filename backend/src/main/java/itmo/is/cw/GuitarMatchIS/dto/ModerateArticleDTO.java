package itmo.is.cw.GuitarMatchIS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ModerateArticleDTO {
   private Long articleId;
   private boolean accepted;
}
