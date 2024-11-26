package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateProductFeedbackDTO {
   @NotNull
   private Long productId;
   @NotBlank
   @Size(min = 1, max = 10000, message = "Text must be between 1 and 10000 characters")
   private String text;
   @NotNull
   @Min(1)
   @Max(5)
   private Integer stars;
}
