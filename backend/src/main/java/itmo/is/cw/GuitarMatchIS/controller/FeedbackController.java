package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import itmo.is.cw.GuitarMatchIS.dto.CreateArticleFeedbackDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateProductFeedbackDTO;
import itmo.is.cw.GuitarMatchIS.dto.FeedbackDTO;
import itmo.is.cw.GuitarMatchIS.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/feedback")
@Tag(name = "Отзывы", description = "API для работы с отзывами на товары и статьи")
public class FeedbackController {
   private final FeedbackService feedbackService;

   @Operation(summary = "Получить отзывы о товаре",
             description = "Возвращает список отзывов для конкретного товара с пагинацией")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Отзывы успешно получены"),
       @ApiResponse(responseCode = "404", description = "Товар не найден")
   })
   @GetMapping("/product/{productId}")
   public List<FeedbackDTO> getFeedbackByProductId(
       @Parameter(description = "ID товара") @PathVariable Long productId,
       @Parameter(description = "Начальная позиция") @RequestParam int from,
       @Parameter(description = "Количество элементов") @RequestParam int size) {
      return feedbackService.getFeedbackByProductId(productId, from, size);
   }

   @Operation(summary = "Получить отзывы о статье",
             description = "Возвращает список отзывов для конкретной статьи с пагинацией")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Отзывы успешно получены"),
       @ApiResponse(responseCode = "404", description = "Статья не найдена")
   })
   @GetMapping("/article/{articleId}")
   public List<FeedbackDTO> getFeedbackByArticleId(
       @Parameter(description = "ID статьи") @PathVariable Long articleId,
       @Parameter(description = "Начальная позиция") @RequestParam int from,
       @Parameter(description = "Количество элементов") @RequestParam int size) {
      return feedbackService.getFeedbackByArticleId(articleId, from, size);
   }

   @Operation(summary = "Добавить отзыв о товаре",
             description = "Создает новый отзыв о товаре")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Отзыв успешно добавлен"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "404", description = "Товар не найден")
   })
   @PostMapping("/product")
   public Boolean addProductFeedback(
       @Parameter(description = "Данные отзыва") @RequestBody @Valid CreateProductFeedbackDTO feedbackDTO,
       HttpServletRequest request) {
      return feedbackService.addProductFeedback(feedbackDTO, request);
   }

   @Operation(summary = "Добавить отзыв о статье",
             description = "Создает новый отзыв о статье")
   @ApiResponses(value = {
       @ApiResponse(responseCode = "200", description = "Отзыв успешно добавлен"),
       @ApiResponse(responseCode = "400", description = "Некорректные данные"),
       @ApiResponse(responseCode = "404", description = "Статья не найдена")
   })
   @PostMapping("/article")
   public Boolean addArticleFeedback(
       @Parameter(description = "Данные отзыва") @RequestBody @Valid CreateArticleFeedbackDTO feedbackDTO,
       HttpServletRequest request) {
      return feedbackService.addArticleFeedback(feedbackDTO, request);
   }
}
