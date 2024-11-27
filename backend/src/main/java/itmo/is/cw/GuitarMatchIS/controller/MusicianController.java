package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.AddProductMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/musician")
@RequiredArgsConstructor
public class MusicianController {
   private final MusicianService musicianService;

   @GetMapping
   public List<MusicianDTO> getMusicians(@RequestParam int from, @RequestParam int size) {
      return musicianService.getMusician(from, size);
   }

   @GetMapping("/name/{name}")
   public List<MusicianDTO> searchMusicians(@PathVariable String name, @RequestParam int from, @RequestParam int size) {
      return musicianService.searchMusicians(name, from, size);
   }

   @GetMapping("/{musicianId}/genres")
   public MusicianGenreDTO getMusiciansByGenre(@PathVariable Long musicianId) {
      return musicianService.getMusiciansByGenre(musicianId);
   }

   @GetMapping("/{musicianId}/types")
   public MusicianTypeOfMusicianDTO getMusiciansByTypeOfMusician(@PathVariable Long musicianId) {
      return musicianService.getMusiciansByTypeOfMusician(musicianId);
   }

   @PostMapping
   public MusicianDTO createMusician(@RequestBody @Valid CreateMusicianDTO createMusicianDTO,
         HttpServletRequest request) {
      return musicianService.createMusician(createMusicianDTO, request);
   }

   @PostMapping("/subscription")
   public Boolean subscribeToMusician(@RequestBody @Valid SubscribeDTO subscribeDTO, HttpServletRequest request) {
      return musicianService.subscribeToMusician(subscribeDTO, request);
   }

   @DeleteMapping("/subscription")
   public Boolean unsubscribeFromMusician(@RequestBody @Valid SubscribeDTO subscribeDTO, HttpServletRequest request) {
      return musicianService.unsubscribeFromMusician(subscribeDTO, request);
   }

   @PostMapping("/product")
   public Boolean addProductToMusician(@RequestBody @Valid AddProductMusicianDTO addProductMusicianDTO,
         HttpServletRequest request) {
      return musicianService.addProductToMusician(addProductMusicianDTO, request);
   }

   @DeleteMapping("/product")
   public Boolean deleteProductFromMusician(@RequestBody @Valid AddProductMusicianDTO addProductMusicianDTO,
         HttpServletRequest request) {
      return musicianService.deleteProductFromMusician(addProductMusicianDTO, request);
   }

   @GetMapping("/{musicianName}/products")
   public MusicianProductDTO getMusicianProducts(@PathVariable String musicianName) {
      return musicianService.getMusicianProducts(musicianName.replaceAll("_", " "));
   }
}
