package itmo.is.cw.GuitarMatchIS.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itmo.is.cw.GuitarMatchIS.dto.AuthResponseDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserDTO;
import itmo.is.cw.GuitarMatchIS.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
   private final AuthService authService;

   @PostMapping("/register")
   public AuthResponseDTO register(@RequestBody @Valid UserDTO registerUserDto) {
      return authService.register(registerUserDto);
   }

   @PostMapping("/login")
   public AuthResponseDTO login(@RequestBody @Valid UserDTO loginUserDto) {
      return authService.login(loginUserDto);
   }
}
