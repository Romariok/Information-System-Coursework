package itmo.is.cw.GuitarMatchIS.genre.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.genre.dao.GenreRepository;
import itmo.is.cw.GuitarMatchIS.genre.dto.CreateGenreDTO;
import itmo.is.cw.GuitarMatchIS.genre.dto.GenreDTO;
import itmo.is.cw.GuitarMatchIS.genre.model.Genre;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.GenreAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class GenreService {
   private final GenreRepository genreRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<GenreDTO> getGenre(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Genre> genres = genreRepository.findAll(page).getContent();

      return genres
            .stream()
            .map(genre1 -> new GenreDTO(
                  genre1.getId(),
                  genre1.getName()))
            .sorted(new Comparator<GenreDTO>() {
               @Override
               public int compare(GenreDTO o1, GenreDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public GenreDTO createGenre(CreateGenreDTO createGenreDTO, HttpServletRequest request) {
      if (genreRepository.existsByName(createGenreDTO.getName()))
         throw new GenreAlreadyExistsException("Genre %s already exists".formatted(createGenreDTO.getName()));

      Genre genre = Genre.builder()
            .name(createGenreDTO.getName())
            .build();

      genre = genreRepository.save(genre);
      simpMessagingTemplate.convertAndSend("/topic", "New genre added");

      return new GenreDTO(genre.getId(), genre.getName());
   }
}
