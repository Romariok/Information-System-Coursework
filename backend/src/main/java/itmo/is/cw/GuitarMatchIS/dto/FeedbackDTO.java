package itmo.is.cw.GuitarMatchIS.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class FeedbackDTO {
   private Long id;
   private UserInfoDTO author;
   private ProductDTO product;
   private ArticleDTO article;
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime createdAt;
   private String text;
   private Integer stars;
}
