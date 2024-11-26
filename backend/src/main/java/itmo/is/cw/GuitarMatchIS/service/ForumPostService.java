package itmo.is.cw.GuitarMatchIS.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.models.ForumPost;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ForumPostRepository;
import itmo.is.cw.GuitarMatchIS.repository.ForumTopicRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ForumPostService {
   private final ForumPostRepository forumPostRepository;
   private final ForumTopicRepository forumTopicRepository;
   private final JwtUtils jwtUtils;
   private final UserRepository userRepository;
   private final SimpMessagingTemplate simpMessagingTemplate;

   public List<ForumPostDTO> getForumPostsByTopic(Long topicId, int from, int size) {
      Pageable pageable = Pagification.createPageTemplate(from, size);

      ForumTopic topic = forumTopicRepository.findById(topicId)
            .orElseThrow(() -> new ForumTopicNotFoundException("Topic with id " + topicId + " not found"));

      List<ForumPost> posts = forumPostRepository.findAllByTopic(topic, pageable).getContent();

      return posts
            .stream()
            .map(this::convertToDTO)
            .sorted(new Comparator<ForumPostDTO>() {
               @Override
               public int compare(ForumPostDTO o1, ForumPostDTO o2) {
                  return o1.getId().compareTo(o2.getId());
               }
            })
            .toList();
   }

   public ForumPostDTO createForumPost(CreateForumPostDTO forumPostDTO, HttpServletRequest request) {
      ForumTopic topic = forumTopicRepository.findById(forumPostDTO.getForumTopicId())
            .orElseThrow(() -> new ForumTopicNotFoundException(
                  String.format("Forum topic with id %s not found", forumPostDTO.getForumTopicId())));
      
      if (jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request)) == null) {
         throw new UserNotFoundException("You are not authorized to create a post");
      }

      User author = findUserByRequest(request);


      ForumPost post = ForumPost.builder()
            .topic(topic)
            .author(author)
            .createdAt(LocalDateTime.now())
            .content(forumPostDTO.getContent())
            .build();
      forumPostRepository.save(post);
      simpMessagingTemplate.convertAndSend("/forum/posts", "Forum post created");

      return convertToDTO(post);
   }

   private ForumPostDTO convertToDTO(ForumPost post) {
      return ForumPostDTO.builder()
            .id(post.getId())
            .forumTopicId(post.getTopic().getId())
            .authorId(post.getAuthor().getId())
            .createdAt(post.getCreatedAt())
            .content(post.getContent())
            .build();
   }

   private User findUserByRequest(HttpServletRequest request) {
      String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
      return userRepository.findByUsername(username).get();
   }
}
