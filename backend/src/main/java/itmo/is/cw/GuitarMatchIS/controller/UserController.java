package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import itmo.is.cw.GuitarMatchIS.dto.AddUserProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Role;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "Пользователи", description = "API для работы с пользователями")
public class UserController {
   private final UserService userService;

   @Operation(summary = "Получить роль пользователя",
             description = "Возвращает роль пользователя по его имени")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Роль успешно получена"),
       @ApiResponse(responseCode = "404", description = "Пользователь не найден")
   })
   @GetMapping("/role/{username}")
   public Role getRoleByUsername(
       @Parameter(description = "Имя пользователя") @PathVariable String username) {
      return userService.getRoleByUsername(username);
   }

   @Operation(summary = "Получить продукты пользователя",
             description = "Возвращает список продуктов, добавленных пользователем")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список продуктов успешно получен"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @GetMapping("/products")
   public List<ProductDTO> getUserProducts(HttpServletRequest request) {
      return userService.getUserProducts(request);
   }

   @Operation(summary = "Добавить продукт пользователю",
             description = "Добавляет продукт в список продуктов пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Продукт успешно добавлен"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @PostMapping("/product")
   public Boolean addProductToUser(HttpServletRequest request, 
       @Parameter(description = "Данные продукта") @RequestBody AddUserProductDTO addUserProductDTO) {
      return userService.addProductToUser(addUserProductDTO, request);
   }

   @Operation(summary = "Удалить продукт у пользователя",
             description = "Удаляет продукт из списка продуктов пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Продукт успешно удален"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @DeleteMapping("/product")
   public Boolean deleteProductFromUser(HttpServletRequest request,
       @Parameter(description = "Данные продукта") @RequestBody AddUserProductDTO addUserProductDTO) {
      return userService.deleteProductFromUser(addUserProductDTO, request);
   }

   @Operation(summary = "Получить жанры пользователя",
             description = "Возвращает список музыкальных жанров пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список жанров успешно получен"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @GetMapping("/genres")
   public List<Genre> getGenresByUser(HttpServletRequest request) {
      return userService.getGenresByUser(request);
   }

   @Operation(summary = "Получить типы музыканта пользователя",
             description = "Возвращает список типов музыканта для пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список типов успешно получен"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @GetMapping("/types")
   public List<TypeOfMusician> getTypesOfMusiciansByUser(HttpServletRequest request) {
      return userService.getTypesOfMusiciansByUser(request);
   }

   @Operation(summary = "Установить жанры пользователю",
             description = "Обновляет список музыкальных жанров пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Жанры успешно обновлены"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @PostMapping("/genres")
   public Boolean setGenresToUser(HttpServletRequest request,
       @Parameter(description = "Список жанров") @RequestBody UserGenreDTO genres) {
      return userService.setGenresToUser(request, genres.getGenres());
   }

   @Operation(summary = "Установить типы музыканта пользователю",
             description = "Обновляет список типов музыканта для пользователя")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Типы успешно обновлены"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @PostMapping("/types")
   public Boolean setTypesOfMusiciansToUser(HttpServletRequest request,
       @Parameter(description = "Список типов музыканта") @RequestBody UserTypeOfMusicianDTO typesOfMusicians) {
      return userService.setTypesOfMusiciansToUser(request, typesOfMusicians.getTypesOfMusicians());
   }
}
