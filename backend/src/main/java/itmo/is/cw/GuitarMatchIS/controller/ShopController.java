package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.dto.ShopProductDTO;
import itmo.is.cw.GuitarMatchIS.service.ShopService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shop")
@Tag(name = "Магазины", description = "API для работы с музыкальными магазинами")
public class ShopController {
   private final ShopService shopService;

   @Operation(summary = "Получить список магазинов", 
             description = "Возвращает список музыкальных магазинов с пагинацией")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список магазинов успешно получен"),
       @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping
   public List<ShopDTO> getShops(
       @Parameter(description = "Начальная позиция") @RequestParam int from,
       @Parameter(description = "Количество элементов") @RequestParam int size) {
      return shopService.getShops(from, size);
   }

   @Operation(summary = "Получить товары магазина",
             description = "Возвращает список товаров конкретного магазина с пагинацией")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
       @ApiResponse(responseCode = "404", description = "Магазин не найден"),
       @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping("/{shopId}/products")
   public ShopProductDTO getShopProducts(
       @Parameter(description = "ID магазина") @PathVariable Long shopId,
       @Parameter(description = "Начальная позиция") @RequestParam int from,
       @Parameter(description = "Количество элементов") @RequestParam int size) {
      return shopService.getShopProducts(shopId, from, size);
   }
}
