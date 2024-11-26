package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianDTO;
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

   @PostMapping
   public MusicianDTO createMusician(@RequestBody @Valid CreateMusicianDTO createMusicianDTO, HttpServletRequest request) {
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
}
