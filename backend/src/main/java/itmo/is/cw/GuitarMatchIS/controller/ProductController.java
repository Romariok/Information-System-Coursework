package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.ProductShopsDTO;
import itmo.is.cw.GuitarMatchIS.models.BodyMaterial;
import itmo.is.cw.GuitarMatchIS.models.Color;
import itmo.is.cw.GuitarMatchIS.models.GuitarForm;
import itmo.is.cw.GuitarMatchIS.models.PickupConfiguration;
import itmo.is.cw.GuitarMatchIS.models.ProductSort;
import itmo.is.cw.GuitarMatchIS.models.TipMaterial;
import itmo.is.cw.GuitarMatchIS.models.TypeComboAmplifier;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import itmo.is.cw.GuitarMatchIS.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/product")
@Tag(name = "Товары", description = "API для работы с музыкальными инструментами и оборудованием")
public class ProductController {
        private final ProductService productService;

        @Operation(summary = "Получить товары по бренду", description = "Возвращает список товаров определенного бренда с пагинацией")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
        })
        @GetMapping()
        public List<ProductGenreDTO> getProductsByBrandName(
                        @Parameter(description = "Название бренда") @RequestParam String brandName,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductsByBrandName(brandName.replaceAll("_", " "), from, size);
        }

        @Operation(summary = "Получить музыкантов по ID товара", description = "Возвращает список музыкантов, которые используют данный товар")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список музыкантов успешно получен"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
        })
        @GetMapping("/{id}/musicians")
        public List<MusicianInfoDTO> getMusiciansByProductId(
                        @Parameter(description = "ID товара") @PathVariable long id,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getMusiciansByProductId(id, from, size);
        }

        @Operation(summary = "Получить товар по id", description = "Возвращает товар по id")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Товар найден"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
        })
        @GetMapping("/id/{id}")
        public ProductGenreDTO getProductsById(
                        @Parameter(description = "id") @PathVariable long id) {
                return productService.getProductsById(id);
        }

        @Operation(summary = "Получить жанры товара", description = "Возвращает список музыкальных жанров, для которых подходит товар")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Жанры успешно получены"),
                        @ApiResponse(responseCode = "404", description = "Товар не найден")
        })
        @GetMapping("/{name}/genres")
        public ProductGenreDTO getGenresByProductName(
                        @Parameter(description = "Название товара") @PathVariable String name) {
                return productService.getGenresByProductName(name.replaceAll("_", " "));
        }

        @Operation(summary = "Получить статьи о товаре", description = "Возвращает список статей, связанных с товаром")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Статьи успешно получены"),
                        @ApiResponse(responseCode = "404", description = "Товар не найден")
        })
        @GetMapping("/{id}/articles")
        public ProductArticleDTO getProductArticles(
                        @Parameter(description = "ID товара") @PathVariable long id,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductArticles(id, from, size);
        }

        @Operation(summary = "Получить магазины, в которых есть товар", description = "Возвращает список магазинов, в которых есть товар")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Магазины успешно получены"),
                        @ApiResponse(responseCode = "404", description = "Товар не найден")
        })
        @GetMapping("/{id}/shops")
        public ProductShopsDTO getProductShops(
                        @Parameter(description = "ID товара") @PathVariable long id,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductShops(id, from, size);
        }

        @Operation(summary = "Получить товары по типу", description = "Возвращает список товаров определенного типа с пагинацией")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Список товаров успешно получен"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
        })
        @GetMapping("/type/{typeOfProduct}")
        public List<ProductGenreDTO> getProductsByTypeOfProduct(
                        @Parameter(description = "Тип товара") @PathVariable TypeOfProduct typeOfProduct,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductsByTypeOfProduct(typeOfProduct, from, size);
        }

        @Operation(summary = "Поиск товаров по названию", description = "Возвращает список товаров, содержащих указанную строку в названии")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Поиск выполнен успешно"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
        })
        @GetMapping("/{name}")
        public List<ProductGenreDTO> getProductsByNameContains(
                        @Parameter(description = "Строка для поиска") @PathVariable String name,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductsByNameContains(name, from, size);
        }

        @Operation(summary = "Фильтрация товаров", description = "Возвращает отфильтрованный список товаров по различным параметрам")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Фильтрация выполнена успешно"),
                        @ApiResponse(responseCode = "400", description = "Некорректные параметры фильтрации")
        })
        @GetMapping("/filter")
        public List<ProductDTO> getProductsByFilter(
                        @Parameter(description = "Название товара") @RequestParam(required = false) String name,
                        @Parameter(description = "Минимальный рейтинг") @RequestParam(required = false) Float minRate,
                        @Parameter(description = "Максимальный рейтинг") @RequestParam(required = false) Float maxRate,
                        @Parameter(description = "ID бренда") @RequestParam(required = false) Long brandId,
                        @Parameter(description = "Форма гитары") @RequestParam(required = false) GuitarForm guitarForm,
                        @Parameter(description = "Тип товара") @RequestParam(required = false) TypeOfProduct typeOfProduct,
                        @Parameter(description = "Количество ладов") @RequestParam(required = false) Integer lads,
                        @Parameter(description = "Минимальная цена") @RequestParam(required = false) Double minPrice,
                        @Parameter(description = "Максимальная цена") @RequestParam(required = false) Double maxPrice,
                        @Parameter(description = "Цвет") @RequestParam(required = false) Color color,
                        @Parameter(description = "Количество струн") @RequestParam(required = false) Integer strings,
                        @Parameter(description = "Материал медиатора") @RequestParam(required = false) TipMaterial tipMaterial,
                        @Parameter(description = "Материал корпуса") @RequestParam(required = false) BodyMaterial bodyMaterial,
                        @Parameter(description = "Конфигурация звукоснимателей") @RequestParam(required = false) PickupConfiguration pickupConfiguration,
                        @Parameter(description = "Тип комбоусилителя") @RequestParam(required = false) TypeComboAmplifier typeComboAmplifier,
                        @Parameter(description = "Сортировка") @RequestParam ProductSort sortBy,
                        @Parameter(description = "Направление сортировки") @RequestParam boolean ascending,
                        @Parameter(description = "Начальная позиция") @RequestParam int from,
                        @Parameter(description = "Количество элементов") @RequestParam int size) {
                return productService.getProductsByFilter(name, minRate, maxRate, brandId, guitarForm, typeOfProduct,
                                lads,
                                minPrice, maxPrice, color, strings, tipMaterial, bodyMaterial, pickupConfiguration,
                                typeComboAmplifier,
                                sortBy, ascending, from, size);
        }
}
