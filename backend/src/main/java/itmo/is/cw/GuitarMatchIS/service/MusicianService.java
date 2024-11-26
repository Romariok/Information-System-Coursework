package itmo.is.cw.GuitarMatchIS.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.models.Musician;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.MusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserMusicianRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MusicianService {
   private final MusicianRepository musicianRepository;
   private final JwtUtils jwtUtils;
   private final UserMusicianRepository userMusicianRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;
   private final UserRepository userRepository;

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
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("You are not authorized to add musician");
      }

      Musician musician = Musician.builder()
            .name(createMusicianDTO.getName())
            .subscribers(0)
            .build();

      musician = musicianRepository.save(musician);
      simpMessagingTemplate.convertAndSend("/musicians", "New musician added");

      return new MusicianDTO(musician.getId(), musician.getName(), musician.getSubscribers());
   }

   public Boolean subscribeToMusician(SubscribeDTO subscribeDTO, HttpServletRequest request) {
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("You are not authorized to subscribe to musician");
      }

      User user = findUserByRequest(request);

      Musician musician = musicianRepository.findById(subscribeDTO.getMusicianId())
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(subscribeDTO.getMusicianId())));

      if (userMusicianRepository.existsByUserAndMusician(user, musician)) {
         throw new SubscriptionAlreadyExistsException("You are already subscribed to this musician");
      }

      userMusicianRepository.subscribeToMusician(user.getId(), subscribeDTO.getMusicianId());
      return true;
   }

   @Transactional
   public Boolean unsubscribeFromMusician(SubscribeDTO subscribeDTO, HttpServletRequest request) {
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("You are not authorized to unsubscribe from musician");
      }

      User user = findUserByRequest(request);

      Musician musician = musicianRepository.findById(subscribeDTO.getMusicianId())
            .orElseThrow(() -> new MusicianNotFoundException(
                  "Musician with id %s not found".formatted(subscribeDTO.getMusicianId())));

      if (!userMusicianRepository.existsByUserAndMusician(user, musician)) {
         throw new SubscriptionNotFoundException("You have no subscription to this musician");
      }

      userMusicianRepository.deleteByUserAndMusician(user, musician);
      return true;
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

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username).get();
   }

}
