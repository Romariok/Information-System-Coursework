package itmo.is.cw.GuitarMatchIS.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserInfoDTO {
   private Long id;
   private String username;
}
