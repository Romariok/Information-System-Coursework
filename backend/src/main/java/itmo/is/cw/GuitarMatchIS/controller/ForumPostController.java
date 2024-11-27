package itmo.is.cw.GuitarMatchIS.controller;

import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.service.ForumPostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/forum/post")
@RequiredArgsConstructor
@Tag(name = "Сообщения форума", description = "API для работы с сообщениями на форуме")
public class ForumPostController {
   private final ForumPostService forumPostService;

   @Operation(summary = "Создание нового сообщения", description = "Создает новое сообщение в теме форума")
   @ApiResponses(value = {
         @ApiResponse(responseCode = "200", description = "Сообщение успешно создано"),
         @ApiResponse(responseCode = "400", description = "Некорректные данные"),
         @ApiResponse(responseCode = "404", description = "Тема форума не найдена")
   })
   @PostMapping
   public ForumPostDTO createForumPost(
         @Parameter(description = "Данные нового сообщения") @RequestBody @Valid CreateForumPostDTO createForumPostDTO,
         HttpServletRequest request) {
      return forumPostService.createForumPost(createForumPostDTO, request);
   }
}
