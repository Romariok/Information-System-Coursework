package itmo.is.cw.GuitarMatchIS.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class ForumTopicDTO {
   private Long id;
   private String title;
   private String description;
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
   private LocalDateTime createdAt;
   private UserInfoDTO author;
   private boolean isClosed;
}
