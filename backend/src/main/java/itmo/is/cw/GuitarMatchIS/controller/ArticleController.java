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

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {
   private final ArticleService articleService;

   @GetMapping
   public List<ArticleDTO> getArticles(@RequestParam int from, @RequestParam int size) {
      return articleService.getArticles(from, size);
   }

   @GetMapping("/unaccepted")
   public List<StatusArticlesDTO> getStatusArticles(@RequestParam int from, @RequestParam int size) {
      return articleService.getStatusArticles(from, size);
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
   public boolean moderateArticle(@RequestBody @Valid ModerateArticleDTO moderateArticleDTO,
         HttpServletRequest request) {
      return articleService.moderateArticle(moderateArticleDTO, request);
   }

   @PostMapping
   public ArticleDTO createArticle(@RequestBody @Valid CreateArticleDTO createArticleDTO, HttpServletRequest request) {
      return articleService.createArticle(createArticleDTO, request);
   }

}
