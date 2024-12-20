package itmo.is.cw.GuitarMatchIS.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.CreateForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ForumTopicRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForumTopicService {
   private final ForumTopicRepository forumTopicRepository;
   private final JwtUtils jwtUtils;
   private final UserRepository userRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<ForumTopicDTO> getForumTopics(int from, int size) {
      Pageable pageable = Pagification.createPageTemplate(from, size);

      List<ForumTopic> topics = forumTopicRepository.findAll(pageable).getContent();

      return topics
            .stream()
            .map(this::convertToDTO)
            .sorted(new Comparator<ForumTopicDTO>() {
               @Override
               public int compare(ForumTopicDTO o1, ForumTopicDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public List<ForumTopicDTO> getForumTopicsByAuthor(Long authorId, int from, int size) {
      User user = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(
                  String.format("User with id %s not found", authorId)));

      Pageable pageable = Pagification.createPageTemplate(from, size);
      List<ForumTopic> topics = forumTopicRepository.findAllByAuthor(user, pageable).getContent();

      return topics
            .stream()
            .map(this::convertToDTO)
            .sorted(new Comparator<ForumTopicDTO>() {
               @Override
               public int compare(ForumTopicDTO o1, ForumTopicDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   @Transactional
   public Boolean closeTopic(Long topicId, HttpServletRequest request) {
      ForumTopic topic = forumTopicRepository.findById(topicId)
            .orElseThrow(() -> new ForumTopicNotFoundException(
                  String.format("Forum topic with id %s not found", topicId)));

      User user = findUserByRequest(request);
      if (!user.getIsAdmin() && !user.getId().equals(topic.getAuthor().getId())) {
         throw new ForbiddenException("You are not authorized to close this topic");
      }

      forumTopicRepository.closeTopic(topic.getId());
      simpMessagingTemplate.convertAndSend("/forum/topics", "Forum topic closed");
      return true;
   }

   @Transactional
   public ForumTopicDTO createTopic(CreateForumTopicDTO createForumTopicDTO, HttpServletRequest request) {
      if (forumTopicRepository.existsByTitle(createForumTopicDTO.getTitle()))
         throw new ForumTopicAlreadyExistsException(String.format("Forum topic with title %s already exists",
               createForumTopicDTO.getTitle()));

      User author = findUserByRequest(request);

      ForumTopic topic = ForumTopic.builder()
            .title(createForumTopicDTO.getTitle())
            .description(createForumTopicDTO.getDescription())
            .author(author)
            .createdAt(LocalDateTime.now())
            .isClosed(false)
            .build();
      forumTopicRepository.save(topic);
      simpMessagingTemplate.convertAndSend("/forum/topics", "Forum topic created");

      return convertToDTO(topic);
   }

   private ForumTopicDTO convertToDTO(ForumTopic topic) {
      return ForumTopicDTO.builder()
            .id(topic.getId())
            .title(topic.getTitle())
            .description(topic.getDescription())
            .createdAt(topic.getCreatedAt())
            .author(UserInfoDTO.builder().id(topic.getAuthor().getId())
                  .username(topic.getAuthor().getUsername()).build())
            .isClosed(topic.getIsClosed())
            .build();
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                  String.format("Username %s not found", username)));
   }

   public Boolean isTopicOwner(Long topicId, HttpServletRequest request) {
      ForumTopic topic = forumTopicRepository.findById(topicId)
            .orElseThrow(() -> new ForumTopicNotFoundException(
                  String.format("Forum topic with id %s not found", topicId)));

      User user = findUserByRequest(request);
      return user.getIsAdmin() || user.getId().equals(topic.getAuthor().getId());
   }
}
