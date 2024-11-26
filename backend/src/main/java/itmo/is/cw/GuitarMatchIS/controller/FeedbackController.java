package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

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
public class FeedbackController {
   private final FeedbackService feedbackService;

   @GetMapping("/product/{productId}")
   public List<FeedbackDTO> getFeedbackByProductId(@PathVariable Long productId, @RequestParam int from,
         @RequestParam int size) {
      return feedbackService.getFeedbackByProductId(productId, from, size);
   }

   @GetMapping("/article/{articleId}")
   public List<FeedbackDTO> getFeedbackByArticleId(@PathVariable Long articleId, @RequestParam int from,
         @RequestParam int size) {
      return feedbackService.getFeedbackByArticleId(articleId, from, size);
   }

   @PostMapping("/product")
   public Boolean addProductFeedback(@RequestBody @Valid CreateProductFeedbackDTO feedbackDTO,
         HttpServletRequest request) {
      return feedbackService.addProductFeedback(feedbackDTO, request);
   }

   @PostMapping("/article")
   public Boolean addArticleFeedback(@RequestBody @Valid CreateArticleFeedbackDTO feedbackDTO,
         HttpServletRequest request) {
      return feedbackService.addArticleFeedback(feedbackDTO, request);
   }
}
