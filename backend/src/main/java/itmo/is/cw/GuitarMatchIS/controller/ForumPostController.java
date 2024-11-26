package itmo.is.cw.GuitarMatchIS.controller;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.service.ForumPostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/forum/post")
@RequiredArgsConstructor
public class ForumPostController {
   private final ForumPostService forumPostService;

   @PostMapping
   public ForumPostDTO createForumPost(@RequestBody @Valid CreateForumPostDTO createForumPostDTO,
         HttpServletRequest request) {
      return forumPostService.createForumPost(createForumPostDTO, request);
   }
}
