package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginUserDTO {

   @NotBlank
   @Size(min = 6, max = 20)
   private String username;

   @NotBlank
   @Size(min = 6, max = 20)
   private String password;

}
