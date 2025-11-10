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
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Аутентификация", description = "API для регистрации и входа пользователей")
public class AuthController {
   private final AuthService authService;

   @Operation(summary = "Регистрация нового пользователя",
             description = "Создает новую учетную запись пользователя в системе")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Успешная регистрация"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
   })
   @PostMapping("/register")
   public AuthResponseDTO register(@RequestBody @Valid UserDTO registerUserDto) {
      log.info("Received request to register user: {}", registerUserDto.getUsername());
      return authService.register(registerUserDto);
   }

   @Operation(summary = "Вход пользователя",
             description = "Аутентифицирует пользователя и возвращает токен доступа")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
   })
   @PostMapping("/login")
   public AuthResponseDTO login(@RequestBody @Valid UserDTO loginUserDto) {
      log.info("Received request to login user: {}", loginUserDto.getUsername());
      return authService.login(loginUserDto);
   }
}
