package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianDTO;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
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
            .map(musician1 -> MusicianDTO.builder()
                  .id(musician1.getId())
                  .name(musician1.getName())
                  .subscribers(musician1.getSubscribers())
                  .build())
            .sorted(new Comparator<MusicianDTO>() {
               @Override
               public int compare(MusicianDTO o1, MusicianDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   @Transactional
   public MusicianDTO createMusician(CreateMusicianDTO createMusicianDTO, HttpServletRequest request) {
      if (musicianRepository.existsByName(createMusicianDTO.getName()))
         throw new MusicianAlreadyExistsException("Musician %s already exists".formatted(createMusicianDTO.getName()));

      Musician musician = Musician.builder()
            .name(createMusicianDTO.getName())
            .subscribers(0)
            .build();

      musician = musicianRepository.save(musician);
      simpMessagingTemplate.convertAndSend("/musicians", "New musician added");

      return new MusicianDTO(musician.getId(), musician.getName(), musician.getSubscribers());
   }

   public List<MusicianDTO> searchMusicians(String name, int from, int size) {
      Pageable page = Pagification.createPageTemplate(from, size);
      List<Musician> musicians = musicianRepository.findAllByNameContains(name, page).getContent();

      return musicians
            .stream()
            .map(musician1 -> MusicianDTO.builder()
                  .id(musician1.getId())
                  .name(musician1.getName())
                  .subscribers(musician1.getSubscribers())
                  .build())
            .sorted(new Comparator<MusicianDTO>() {
               @Override
               public int compare(MusicianDTO o1, MusicianDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

}
