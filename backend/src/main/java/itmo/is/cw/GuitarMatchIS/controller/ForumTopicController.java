package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
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
public class ForumTopicController {
   private final ForumTopicService forumTopicService;
   private final ForumPostService forumPostService;

   @GetMapping
   public List<ForumTopicDTO> getForumTopics(@RequestParam int from, @RequestParam int size) {
      return forumTopicService.getForumTopics(from, size);
   }

   @GetMapping("/author/{authorId}")
   public List<ForumTopicDTO> getForumTopicsByAuthor(@PathVariable Long authorId, @RequestParam int from,
         @RequestParam int size) {
      return forumTopicService.getForumTopicsByAuthor(authorId, from, size);
   }

   @PostMapping
   public ForumTopicDTO createForumTopic(@RequestBody @Valid CreateForumTopicDTO createForumTopicDTO,
         HttpServletRequest request) {
      return forumTopicService.createTopic(createForumTopicDTO, request);
   }

   @PutMapping("/{topicId}/close")
   public Boolean closeForumTopic(@PathVariable Long topicId, HttpServletRequest request) {
      return forumTopicService.closeTopic(topicId, request);
   }

   @GetMapping("/{topicId}/posts")
   public List<ForumPostDTO> getForumPostsByTopic(@PathVariable Long topicId, @RequestParam int from,
         @RequestParam int size) {
      return forumPostService.getForumPostsByTopic(topicId, from, size);
   }
}
