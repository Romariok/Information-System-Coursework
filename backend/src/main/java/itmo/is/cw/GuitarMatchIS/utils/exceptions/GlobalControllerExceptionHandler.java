package itmo.is.cw.GuitarMatchIS.utils.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleUserAlreadyExistException(UserAlreadyExistException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleProductUserAlreadyExistsException(ProductUserAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleProductUserNotFoundException(ProductUserNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleProductMusicianNotFoundException(ProductMusicianNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleProductMusicianAlreadyExists(ProductMusicianAlreadyExists e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleBrandNotFoundException(BrandNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleShopNotFoundException(ShopNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleSubscriptionNotFoundException(SubscriptionNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleMusicianNotFoundException(MusicianNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleSubscriptionAlreadyExistsException(SubscriptionAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleProductNotFoundException(ProductNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleUserNotFoundException(UserNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleForumTopicAlreadyExistsException(ForumTopicAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleForumPostAlreadyExists(ForumPostAlreadyExists e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleArticleAlreadyExistsException(ArticleAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleArticleNotFoundException(ArticleNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleForumTopicNotFoundException(ForumTopicNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleMForbiddenException(ForbiddenException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleIllegalArgumentException(IllegalArgumentException e) {
            return new ErrorResponse(
                        e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleMusicianAlreadyExistsException(MusicianAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler(GenreAlreadyExistsException.class)
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleGenreAlreadyExistsException(GenreAlreadyExistsException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler(GenreNotFoundException.class)
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleGenreNotFoundException(GenreNotFoundException e) {
            return new ErrorResponse(e.getClass().getCanonicalName(),
                        e.getMessage());
      }

      @ExceptionHandler
      @ResponseStatus(HttpStatus.BAD_REQUEST)
      public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
            List<String> errors = e.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error -> String.format(
                                    "Ошибка валидации поля '%s': %s (текущее значение: '%s')",
                                    error.getField(),
                                    error.getDefaultMessage(),
                                    error.getRejectedValue() == null ? "пусто" : error.getRejectedValue()))
                        .collect(Collectors.toList());

            return new ErrorResponse(
                        "Ошибка валидации",
                        String.join("\n", errors));
      }
}
