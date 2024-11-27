package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.service.ForumPostService;
import itmo.is.cw.GuitarMatchIS.service.ForumTopicService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/forum/topic")
@RequiredArgsConstructor
@Tag(name = "Темы форума", description = "API для работы с темами на форуме")
public class ForumTopicController {
   private final ForumTopicService forumTopicService;
   private final ForumPostService forumPostService;

   @Operation(summary = "Получить список тем", description = "Возвращает список тем форума с пагинацией")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Темы успешно получены"),
         @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса")
   })
   @GetMapping
   public List<ForumTopicDTO> getForumTopics(
         @Parameter(description = "Начальная позиция") @RequestParam int from,
         @Parameter(description = "Количество элементов") @RequestParam int size) {
      return forumTopicService.getForumTopics(from, size);
   }

   @Operation(summary = "Получить темы автора", description = "Возвращает список тем, созданных конкретным автором")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Темы автора успешно получены"),
         @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
         @ApiResponse(responseCode = "404", description = "Автор не найден")
   })
   @GetMapping("/author/{authorId}")
   public List<ForumTopicDTO> getForumTopicsByAuthor(
         @Parameter(description = "ID автора") @PathVariable Long authorId,
         @Parameter(description = "Начальная позиция") @RequestParam int from,
         @Parameter(description = "Количество элементов") @RequestParam int size) {
      return forumTopicService.getForumTopicsByAuthor(authorId, from, size);
   }

   @Operation(summary = "Создать новую тему", description = "Создает новую тему на форуме")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Тема успешно создана"),
         @ApiResponse(responseCode = "400", description = "Некорректные данные"),
         @ApiResponse(responseCode = "401", description = "Не авторизован")
   })
   @PostMapping
   public ForumTopicDTO createForumTopic(
         @Parameter(description = "Данные новой темы") @RequestBody @Valid CreateForumTopicDTO createForumTopicDTO,
         HttpServletRequest request) {
      return forumTopicService.createTopic(createForumTopicDTO, request);
   }

   @Operation(summary = "Закрыть тему", description = "Закрывает тему форума, запрещая новые сообщения")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Тема успешно закрыта"),
         @ApiResponse(responseCode = "400", description = "Некорректный ID темы"),
         @ApiResponse(responseCode = "401", description = "Не авторизован"),
         @ApiResponse(responseCode = "403", description = "Нет прав на закрытие темы")
   })
   @PutMapping("/{topicId}/close")
   public Boolean closeForumTopic(
         @Parameter(description = "ID темы") @PathVariable Long topicId,
         HttpServletRequest request) {
      return forumTopicService.closeTopic(topicId, request);
   }

   @Operation(summary = "Получить сообщения темы", description = "Возвращает список сообщений в теме с пагинацией")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Сообщения успешно получены"),
         @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
         @ApiResponse(responseCode = "404", description = "Тема не найдена")
   })
   @GetMapping("/{topicId}/posts")
   public List<ForumPostDTO> getForumPostsByTopic(
         @Parameter(description = "ID темы") @PathVariable Long topicId,
         @Parameter(description = "Начальная позиция") @RequestParam int from,
         @Parameter(description = "Количество элементов") @RequestParam int size) {
      return forumPostService.getForumPostsByTopic(topicId, from, size);
   }
}
