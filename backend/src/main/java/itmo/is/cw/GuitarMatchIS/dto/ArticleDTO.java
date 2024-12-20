package itmo.is.cw.GuitarMatchIS.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ArticleDTO {
   private Long id;
   private String header;
   private String text;
   private String htmlContent;
   private UserInfoDTO author;
   private LocalDateTime createdAt;
   private Boolean accepted;
}
