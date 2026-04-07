package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ForumPostRepository;
import itmo.is.cw.GuitarMatchIS.repository.ForumTopicRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumPostServiceTest {

    @Mock
    private ForumPostRepository forumPostRepository;

    @Mock
    private ForumTopicRepository forumTopicRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private ForumPostService forumPostService;

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void createPost_closedTopic_throwsForumTopicNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        ForumTopic closedTopic = TestDataFactory.buildClosedForumTopic();
        mockFindUserByRequest(request, author);
        when(forumTopicRepository.findById(2L)).thenReturn(Optional.of(closedTopic));

        ForumTopicNotFoundException ex = assertThrows(ForumTopicNotFoundException.class,
                () -> forumPostService.createForumPost(
                        new CreateForumPostDTO(2L, "My post content"),
                        request));

        assertThat(ex.getMessage()).contains("closed");
    }

    @Test
    void createPost_topicNotFound_throwsForumTopicNotFoundException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(forumTopicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ForumTopicNotFoundException.class,
                () -> forumPostService.createForumPost(
                        new CreateForumPostDTO(99L, "My post content"),
                        request));
    }

    @Test
    void getPostsByTopicId_topicNotFound_throwsForumTopicNotFoundException() {
        when(forumTopicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ForumTopicNotFoundException.class,
                () -> forumPostService.getForumPostsByTopic(99L, 0, 10));
    }

    @Test
    void getPostsByTopicId_openTopic_returnsPostList() {
        ForumTopic topic = TestDataFactory.buildForumTopic();
        when(forumTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        when(forumPostRepository.findAllByTopic(eq(topic), any()))
                .thenReturn(new PageImpl<>(List.of(TestDataFactory.buildForumPost())));

        List<ForumPostDTO> result = forumPostService.getForumPostsByTopic(1L, 0, 10);

        assertThat(result).hasSize(1);
    }
}
