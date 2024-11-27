package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.BrandDTO;
import itmo.is.cw.GuitarMatchIS.service.BrandService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brand")
@Tag(name = "Бренды", description = "API для работы с брендами музыкальных инструментов")
public class BrandController {
   private final BrandService brandService;

   @Operation(summary = "Получить список брендов", 
             description = "Возвращает список брендов с пагинацией")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Список брендов успешно получен"),
       @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping
   public List<BrandDTO> getBrands(
       @Parameter(description = "Начальная позиция") @RequestParam int from,
       @Parameter(description = "Количество элементов") @RequestParam int size) {
      return brandService.getBrands(from, size);
   }
}
