package itmo.is.cw.GuitarMatchIS.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDTO {

   @NotBlank
   @Size(min = 5, max = 20, message = "Username must be between 5 and 20 characters")
   private String username;

   @NotBlank
   @Size(min = 5, max = 20, message = "Password must be between 5 and 20 characters")
   private String password;

}
