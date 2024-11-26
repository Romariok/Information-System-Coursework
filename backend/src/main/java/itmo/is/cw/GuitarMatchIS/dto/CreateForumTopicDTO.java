package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder(toBuilder = true)
public class CreateForumTopicDTO {
   @NotBlank
   @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
   private String title;
   @NotBlank
   @Size(min = 1, max = 1000, message = "Description must be between 1 and 1000 characters")
   private String description;
}
