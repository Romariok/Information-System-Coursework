package itmo.is.cw.GuitarMatchIS.musician.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.musician.dao.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.musician.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.musician.dto.MusicianDTO;
import itmo.is.cw.GuitarMatchIS.musician.model.Musician;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MusicianService {
   private final MusicianRepository musicianRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<MusicianDTO> getMusician(int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);

      List<Musician> musicians = musicianRepository.findAll(page).getContent();

      return musicians
            .stream()
            .map(musician1 -> new MusicianDTO(
                  musician1.getId(),
                  musician1.getName(),
                  musician1.getSubscribers()))
            .sorted(new Comparator<MusicianDTO>() {
               @Override
               public int compare(MusicianDTO o1, MusicianDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public MusicianDTO createMusician(CreateMusicianDTO createMusicianDTO, HttpServletRequest request) {
      if (musicianRepository.existsByName(createMusicianDTO.getName()))
         throw new MusicianAlreadyExistsException("Musician %s already exists".formatted(createMusicianDTO.getName()));

      Musician musician = Musician.builder()
            .name(createMusicianDTO.getName())
            .subscribers(0)
            .build();

      musician = musicianRepository.save(musician);
      simpMessagingTemplate.convertAndSend("/topic", "New musician added");

      return new MusicianDTO(musician.getId(), musician.getName(), musician.getSubscribers());
   }

}