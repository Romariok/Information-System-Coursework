package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
   private final ArticleService articleService;

   @GetMapping
   public List<ArticleDTO> getArticles(@RequestParam int from, @RequestParam int size) {
      return articleService.getArticles(from, size);
   }

   @GetMapping("/header/{header}")
   public List<ArticleDTO> getArticlesByHeaderContaining(@PathVariable String header, @RequestParam int from,
         @RequestParam int size) {
      return articleService.getArticlesByHeaderContaining(header, from, size);
   }

   @GetMapping("/author/{authorId}")
   public List<ArticleDTO> getArticlesByAuthorId(@PathVariable Long authorId, @RequestParam int from,
         @RequestParam int size) {
      return articleService.getArticlesByAuthorId(authorId, from, size);
   }

   @PostMapping("/moderate")
   public boolean moderateArticle(@RequestParam Long articleId, @RequestParam boolean accepted,
         HttpServletRequest request) {
      return articleService.moderateArticle(articleId, accepted, request);
   }

   @PostMapping
   public ArticleDTO createArticle(@RequestBody @Valid CreateArticleDTO createArticleDTO, HttpServletRequest request) {
      return articleService.createArticle(createArticleDTO, request);
   }

}
