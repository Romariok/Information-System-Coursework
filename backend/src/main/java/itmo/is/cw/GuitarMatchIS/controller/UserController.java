package itmo.is.cw.GuitarMatchIS.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

import itmo.is.cw.GuitarMatchIS.dto.UserGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.Role;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
   private final UserService userService;

   @GetMapping("/role/{username}")
   public Role getRoleByUsername(@PathVariable String username) {
      return userService.getRoleByUsername(username);
   }

   @GetMapping("/genres")
   public List<Genre> getGenresByUser(HttpServletRequest request) {
      return userService.getGenresByUser(request);
   }

   @GetMapping("/types")
   public List<TypeOfMusician> getTypesOfMusiciansByUser(HttpServletRequest request) {
      return userService.getTypesOfMusiciansByUser(request);
   }

   @PostMapping("/genres")
   public Boolean setGenresToUser(HttpServletRequest request, @RequestBody UserGenreDTO genres) {
      return userService.setGenresToUser(request, genres.getGenres());
   }

   @PostMapping("/types")
   public Boolean setTypesOfMusiciansToUser(HttpServletRequest request,
         @RequestBody UserTypeOfMusicianDTO typesOfMusicians) {
      return userService.setTypesOfMusiciansToUser(request, typesOfMusicians.getTypesOfMusicians());
   }
}
