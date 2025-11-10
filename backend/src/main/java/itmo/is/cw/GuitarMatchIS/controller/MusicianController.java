package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.AddProductMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.models.MusicianSort;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/musician")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Музыканты", description = "API для работы с музыкантами")
public class MusicianController {
   private final MusicianService musicianService;


   @Operation(summary = "Получить информацию о музыканте", description = "Возвращает информацию о музыканте по его ID")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Информация о музыканте успешно получена"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден")
   })
   @GetMapping("/id/{id}")
   public MusicianInfoDTO getMusicianInfo(@PathVariable Long id) {
      log.info("Request for musician info with id {} received.", id);
      return musicianService.getMusicianInfo(id);
   }

   @Operation(summary = "Получить список музыкантов", description = "Возвращает список музыкантов с пагинацией")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Список музыкантов успешно получен"),
         @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping
   public List<MusicianInfoDTO> getMusicians(
         @Parameter(description = "Сортировка") @RequestParam MusicianSort sortBy,
         @Parameter(description = "Направление сортировки") @RequestParam boolean ascending,
         @Parameter(description = "Начальная позиция") @RequestParam int from,
         @Parameter(description = "Количество элементов") @RequestParam int size) {
      log.info("Request for musicians received. from={}, size={}, sortBy={}, ascending={}", from, size, sortBy, ascending);
      return musicianService.getMusician(from, size, sortBy, ascending);
   }

   @Operation(summary = "Проверить, подписан ли пользователь на музыканта", description = "Возвращает true, если пользователь подписан на музыканта, и false в противном случае")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Проверка выполнена успешно"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден")
   })
   @GetMapping("/{musicianId}/subscription")
   public Boolean isSubscribed(@PathVariable Long musicianId, HttpServletRequest request) {
      log.info("Request to check subscription for musician with id {} received.", musicianId);
      return musicianService.isSubscribed(musicianId, request);
   }

   @Operation(summary = "Поиск музыкантов по имени", description = "Возвращает список музыкантов, чьи имена содержат указанную строку")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Поиск выполнен успешно"),
         @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping("/name/{name}")
   public List<MusicianInfoDTO> searchMusicians(
         @Parameter(description = "Имя для поиска") @PathVariable String name,
         @Parameter(description = "Начальная позиция") @RequestParam int from,
         @Parameter(description = "Количество элементов") @RequestParam int size) {
      log.info("Request to search musicians with name containing '{}' received. from={}, size={}", name, from, size);
      return musicianService.searchMusicians(name, from, size);
   }

   @Operation(summary = "Получить жанры музыканта", description = "Возвращает список жанров, в которых работает указанный музыкант")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Жанры успешно получены"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден")
   })
   @GetMapping("/{musicianId}/genres")
   public MusicianGenreDTO getGenresByMusician(
         @Parameter(description = "ID музыканта") @PathVariable Long musicianId) {
      log.info("Request for genres of musician with id {} received.", musicianId);
      return musicianService.getMusiciansByGenre(musicianId);
   }

   @Operation(summary = "Получить типы музыканта", description = "Возвращает список типов (ролей) указанного музыканта")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Типы успешно получены"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден")
   })
   @GetMapping("/{musicianId}/types")
   public MusicianTypeOfMusicianDTO getMusiciansByTypeOfMusician(
         @Parameter(description = "ID музыканта") @PathVariable Long musicianId) {
      log.info("Request for types of musician with id {} received.", musicianId);
      return musicianService.getMusiciansByTypeOfMusician(musicianId);
   }

   @Operation(summary = "Создать музыканта", description = "Создает нового музыканта в системе")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Музыкант успешно создан"),
         @ApiResponse(responseCode = "400", description = "Некорректные данные"),
         @ApiResponse(responseCode = "409", description = "Музыкант уже существует")
   })
   @PostMapping
   public MusicianInfoDTO createMusician(
         @Parameter(description = "Данные музыканта") @RequestBody @Valid CreateMusicianDTO createMusicianDTO,
         HttpServletRequest request) {
      log.info("Request to create musician with name '{}' received.", createMusicianDTO.getName());
      return musicianService.createMusician(createMusicianDTO, request);
   }

   @Operation(summary = "Подписаться на музыканта", description = "Создает подписку текущего пользователя на указанного музыканта")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Подписка успешно создана"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден"),
         @ApiResponse(responseCode = "409", description = "Подписка уже существует")
   })
   @PostMapping("/subscription")
   public Boolean subscribeToMusician(
         @Parameter(description = "Данные подписки") @RequestBody @Valid SubscribeDTO subscribeDTO,
         HttpServletRequest request) {
      log.info("Request to subscribe to musician with id {} received.", subscribeDTO.getMusicianId());
      return musicianService.subscribeToMusician(subscribeDTO, request);
   }

   @Operation(summary = "Отписаться от музыканта", description = "Удаляет подписку текущего пользователя на указанного музыканта")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Подписка успешно удалена"),
         @ApiResponse(responseCode = "404", description = "Подписка не найдена")
   })
   @DeleteMapping("/subscription")
   public Boolean unsubscribeFromMusician(
         @Parameter(description = "Данные подписки") @RequestBody @Valid SubscribeDTO subscribeDTO,
         HttpServletRequest request) {
      log.info("Request to unsubscribe from musician with id {} received.", subscribeDTO.getMusicianId());
      return musicianService.unsubscribeFromMusician(subscribeDTO, request);
   }

   @Operation(summary = "Добавить продукт музыканту", description = "Связывает продукт с музыкантом")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Продукт успешно добавлен"),
         @ApiResponse(responseCode = "404", description = "Музыкант или продукт не найден"),
         @ApiResponse(responseCode = "409", description = "Связь уже существует")
   })
   @PostMapping("/product")
   public Boolean addProductToMusician(
         @Parameter(description = "Данные связи продукта с музыкантом") @RequestBody @Valid AddProductMusicianDTO addProductMusicianDTO,
         HttpServletRequest request) {
      log.info("Request to add product with id {} to musician '{}' received.", addProductMusicianDTO.getProductId(), addProductMusicianDTO.getMusicianName());
      return musicianService.addProductToMusician(addProductMusicianDTO, request);
   }

   @Operation(summary = "Удалить продукт у музыканта", description = "Удаляет связь продукта с музыкантом")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Продукт успешно удален"),
         @ApiResponse(responseCode = "404", description = "Связь не найдена")
   })
   @DeleteMapping("/product")
   public Boolean deleteProductFromMusician(
         @Parameter(description = "Данные связи продукта с музыкантом") @RequestBody @Valid AddProductMusicianDTO addProductMusicianDTO,
         HttpServletRequest request) {
      log.info("Request to delete product with id {} from musician '{}' received.", addProductMusicianDTO.getProductId(), addProductMusicianDTO.getMusicianName());
      return musicianService.deleteProductFromMusician(addProductMusicianDTO, request);
   }

   @Operation(summary = "Получить продукты музыканта", description = "Возвращает список продуктов, связанных с указанным музыкантом")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Продукты успешно получены"),
         @ApiResponse(responseCode = "404", description = "Музыкант не найден")
   })
   @GetMapping("/{musicianName}/products")
   public MusicianProductDTO getMusicianProducts(
         @Parameter(description = "Имя музыканта") @PathVariable String musicianName) {
      log.info("Request for products of musician with name '{}' received.", musicianName);
      return musicianService.getMusicianProducts(musicianName.replaceAll("_", " "));
   }
}
