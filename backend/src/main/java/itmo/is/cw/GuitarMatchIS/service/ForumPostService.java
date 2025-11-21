package itmo.is.cw.GuitarMatchIS.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import itmo.is.cw.GuitarMatchIS.Pagification;
import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.models.ForumPost;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ForumPostRepository;
import itmo.is.cw.GuitarMatchIS.repository.ForumTopicRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForumPostService {
      private final ForumPostRepository forumPostRepository;
      private final ForumTopicRepository forumTopicRepository;
      private final JwtUtils jwtUtils;
      private final UserRepository userRepository;
      private final SimpMessagingTemplate simpMessagingTemplate;

      public List<ForumPostDTO> getForumPostsByTopic(Long topicId, int from, int size) {
            log.info("Fetching forum posts for topic with id: {}", topicId);
            Pageable pageable = Pagification.createPageTemplate(from, size);

            ForumTopic topic = forumTopicRepository.findById(topicId)
                        .orElseThrow(() -> {
                              log.warn("Forum topic with id {} not found", topicId);
                              return new ForumTopicNotFoundException("Topic with id " + topicId + " not found");
                        });
            List<ForumPost> posts = forumPostRepository.findAllByTopic(topic, pageable).getContent();

            return posts
                        .stream()
                        .map(this::convertToDTO)
                        .sorted(Comparator.comparing(ForumPostDTO::getId))
                        .toList();
      }

      public ForumPostDTO createForumPost(CreateForumPostDTO forumPostDTO, HttpServletRequest request) {
            log.info("Creating forum post for topic with id: {}", forumPostDTO.getForumTopicId());
            ForumTopic topic = forumTopicRepository.findById(forumPostDTO.getForumTopicId())
                        .orElseThrow(() -> {
                              log.warn("Forum topic with id {} not found while creating post",
                                          forumPostDTO.getForumTopicId());
                              return new ForumTopicNotFoundException(
                                          String.format("Forum topic with id %s not found",
                                                      forumPostDTO.getForumTopicId()));
                        });
            User author = findUserByRequest(request);
            log.info("Forum post author is {}", author.getUsername());
            if (topic.getIsClosed()) {
                  log.warn("Attempt to create post in closed topic with id {}", topic.getId());
                  throw new ForumTopicNotFoundException("This topic is closed");
            }
            ForumPost post = ForumPost.builder()
                        .topic(topic)
                        .author(author)
                        .createdAt(LocalDateTime.now())
                        .content(forumPostDTO.getContent())
                        .build();
            forumPostRepository.save(post);
            log.info("Forum post with id {} successfully created", post.getId());
            simpMessagingTemplate.convertAndSend("/forum/posts", "Forum post created");

            return convertToDTO(post);
      }

      private ForumPostDTO convertToDTO(ForumPost post) {
            return ForumPostDTO.builder()
                        .id(post.getId())
                        .forumTopicId(post.getTopic().getId())
                        .author(UserInfoDTO.builder().id(post.getAuthor().getId())
                                    .username(post.getAuthor().getUsername()).build())
                        .createdAt(post.getCreatedAt())
                        .content(post.getContent())
                        .build();
      }

      private User findUserByRequest(HttpServletRequest request) {
            String username = jwtUtils.getUserNameFromJwtToken(jwtUtils.parseJwt(request));
            return userRepository.findByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                    String.format("Username %s not found", username)));
      }
}
