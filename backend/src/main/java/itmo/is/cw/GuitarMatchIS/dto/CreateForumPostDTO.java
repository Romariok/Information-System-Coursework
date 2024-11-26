package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class CreateForumPostDTO {
   @Positive
   private Long forumTopicId;
   @NotBlank
   @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
   private String content;
}
