package itmo.is.cw.GuitarMatchIS.genre.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.genre.dto.CreateGenreDTO;
import itmo.is.cw.GuitarMatchIS.genre.dto.GenreDTO;
import itmo.is.cw.GuitarMatchIS.genre.service.GenreService;

import java.util.List;

@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor
public class GenreController {
   private final GenreService genreService;

   @GetMapping
   public List<GenreDTO> getGenres(@RequestParam int from, @RequestParam int size) {
      return genreService.getGenre(from, size);
   }

   @PostMapping
   public GenreDTO createGenre(@RequestBody @Valid CreateGenreDTO createGenreDTO,
         HttpServletRequest request) {
      return genreService.createGenre(createGenreDTO, request);
   }
}
