package itmo.is.cw.GuitarMatchIS.service;

import itmo.is.cw.GuitarMatchIS.dto.CreateForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.models.ForumTopic;
import itmo.is.cw.GuitarMatchIS.models.User;
import itmo.is.cw.GuitarMatchIS.repository.ForumTopicRepository;
import itmo.is.cw.GuitarMatchIS.repository.UserRepository;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.utils.TestDataFactory;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForumTopicServiceTest {

    @Mock
    private ForumTopicRepository forumTopicRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @InjectMocks
    private ForumTopicService forumTopicService;

    private void mockFindUserByRequest(HttpServletRequest request, User user) {
        when(jwtUtils.parseJwt(request)).thenReturn("test-token");
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn(user.getUsername());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void createTopic_duplicate_throwsForumTopicAlreadyExistsException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(forumTopicRepository.existsByTitle("Duplicate Topic")).thenReturn(true);

        assertThrows(ForumTopicAlreadyExistsException.class,
                () -> forumTopicService.createTopic(
                        new CreateForumTopicDTO("Duplicate Topic", "Some description"),
                        request));

        verify(forumTopicRepository, never()).save(any(ForumTopic.class));
    }

    @Test
    void createTopic_success_returnsSavedTopic() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        ForumTopic savedTopic = TestDataFactory.buildForumTopic();
        mockFindUserByRequest(request, author);
        when(forumTopicRepository.existsByTitle("New Topic")).thenReturn(false);
        when(forumTopicRepository.save(any(ForumTopic.class))).thenReturn(savedTopic);
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        ForumTopicDTO result = forumTopicService.createTopic(
                new CreateForumTopicDTO("New Topic", "Topic description"),
                request);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Topic");
        verify(forumTopicRepository, times(1)).save(any(ForumTopic.class));
    }

    @Test
    void getTopics_withPagination_returnsList() {
        when(forumTopicRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(TestDataFactory.buildForumTopic())));

        List<ForumTopicDTO> result = forumTopicService.getForumTopics(0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getTopics_emptyNameFilter_returnsEmptyList() {
        when(forumTopicRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        List<ForumTopicDTO> result = forumTopicService.getForumTopics(0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    void closeTopic_notOwnerOrAdmin_throwsForbiddenException() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        User otherUser = User.builder().id(99L).username("other").isAdmin(false).build();
        ForumTopic topic = TestDataFactory.buildForumTopic();
        topic.setAuthor(author);
        when(forumTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        mockFindUserByRequest(request, otherUser);

        assertThrows(ForbiddenException.class,
                () -> forumTopicService.closeTopic(1L, request));
    }

    @Test
    void closeTopic_success_closesTopic() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User admin = TestDataFactory.buildAdmin();
        ForumTopic topic = TestDataFactory.buildForumTopic();
        when(forumTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        mockFindUserByRequest(request, admin);
        when(forumTopicRepository.closeTopic(1L)).thenReturn(1);
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), anyString());

        Boolean result = forumTopicService.closeTopic(1L, request);

        assertThat(result).isTrue();
        verify(forumTopicRepository, times(1)).closeTopic(1L);
    }

    @Test
    void getForumTopicById_notFound_throwsForumTopicNotFoundException() {
        when(forumTopicRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ForumTopicNotFoundException.class,
                () -> forumTopicService.getForumTopicById(99L));
    }

    @Test
    void isTopicOwner_true_returnsTrue() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        ForumTopic topic = TestDataFactory.buildForumTopic();
        topic.setAuthor(author);
        when(forumTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        mockFindUserByRequest(request, author);

        Boolean result = forumTopicService.isTopicOwner(1L, request);

        assertThat(result).isTrue();
    }

    @Test
    void isTopicOwner_false_returnsFalse() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        User author = TestDataFactory.buildUser();
        User otherUser = User.builder().id(99L).username("other").isAdmin(false).build();
        ForumTopic topic = TestDataFactory.buildForumTopic();
        topic.setAuthor(author);
        when(forumTopicRepository.findById(1L)).thenReturn(Optional.of(topic));
        mockFindUserByRequest(request, otherUser);

        Boolean result = forumTopicService.isTopicOwner(1L, request);

        assertThat(result).isFalse();
    }

    @Test
    void getForumTopicsByAuthor_returnsList() {
        User author = TestDataFactory.buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(forumTopicRepository.findAllByAuthor(eq(author), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(TestDataFactory.buildForumTopic())));

        List<ForumTopicDTO> result = forumTopicService.getForumTopicsByAuthor(1L, 0, 10);

        assertThat(result).hasSize(1);
    }

    @Test
    void getForumTopicsByAuthor_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> forumTopicService.getForumTopicsByAuthor(99L, 0, 10));
    }
}
