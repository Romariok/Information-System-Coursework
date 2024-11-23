package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateArticleDTO {
   @NotBlank
   @Size(min = 1, max = 255, message = "Header must be between 1 and 255 characters")
   private String header;
   @NotBlank
   @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
   private String text;
   @NotNull
   private Boolean accepted;
}
