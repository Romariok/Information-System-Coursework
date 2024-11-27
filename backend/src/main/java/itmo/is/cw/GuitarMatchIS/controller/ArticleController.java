package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ModerateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.StatusArticlesDTO;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "Статьи", description = "API для работы со статьями")
public class ArticleController {
   private final ArticleService articleService;

   @Operation(summary = "Получить список статей", 
             description = "Возвращает список статей с пагинацией")
   @GetMapping
   public List<ArticleDTO> getArticles(
      @Parameter(description = "Начальная позиция") @RequestParam int from,
      @Parameter(description = "Количество элементов") @RequestParam int size) {
      return articleService.getArticles(from, size);
   }

   @Operation(summary = "Получить список неодобренных статей",
             description = "Возвращает список статей, ожидающих модерации")
   @GetMapping("/unaccepted")
   public List<StatusArticlesDTO> getStatusArticles(
      @Parameter(description = "Начальная позиция") @RequestParam int from,
      @Parameter(description = "Количество элементов") @RequestParam int size) {
      return articleService.getStatusArticles(from, size);
   }

   @Operation(summary = "Поиск статей по заголовку",
             description = "Возвращает список статей, содержащих указанный текст в заголовке")
   @GetMapping("/header/{header}")
   public List<ArticleDTO> getArticlesByHeaderContaining(
      @Parameter(description = "Текст для поиска в заголовке") @PathVariable String header,
      @Parameter(description = "Начальная позиция") @RequestParam int from,
      @Parameter(description = "Количество элементов") @RequestParam int size) {
      return articleService.getArticlesByHeaderContaining(header, from, size);
   }

   @Operation(summary = "Получить статьи автора",
             description = "Возвращает список статей определенного автора")
   @GetMapping("/author/{authorId}")
   public List<ArticleDTO> getArticlesByAuthorId(
      @Parameter(description = "ID автора") @PathVariable Long authorId,
      @Parameter(description = "Начальная позиция") @RequestParam int from,
      @Parameter(description = "Количество элементов") @RequestParam int size) {
      return articleService.getArticlesByAuthorId(authorId, from, size);
   }

   @Operation(summary = "Модерация статьи",
             description = "Одобрение или отклонение статьи модератором")
   @PostMapping("/moderate")
   public boolean moderateArticle(
      @Parameter(description = "Данные для модерации") @RequestBody @Valid ModerateArticleDTO moderateArticleDTO,
      HttpServletRequest request) {
      return articleService.moderateArticle(moderateArticleDTO, request);
   }

   @Operation(summary = "Создание новой статьи",
             description = "Создание новой статьи пользователем")
   @PostMapping
   public ArticleDTO createArticle(
      @Parameter(description = "Данные новой статьи") @RequestBody @Valid CreateArticleDTO createArticleDTO,
      HttpServletRequest request) {
      return articleService.createArticle(createArticleDTO, request);
   }
}
