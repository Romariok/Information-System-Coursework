package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.CreateForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ForumPostService;
import itmo.is.cw.GuitarMatchIS.service.ForumTopicService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicAlreadyExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import itmo.is.cw.GuitarMatchIS.dto.ForumPostDTO;
import itmo.is.cw.GuitarMatchIS.dto.ForumTopicDTO;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForumTopicNotFoundException;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForumTopicController.class)
@Import(TestWebMvcSecurityConfig.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ForumTopicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ForumTopicService forumTopicService;

    @MockitoBean
    private ForumPostService forumPostService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getForumTopics_withParams_returns200() throws Exception {
        when(forumTopicService.getForumTopics(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/forum/topic")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void createForumTopic_unauthenticated_returns401() throws Exception {
        CreateForumTopicDTO dto = CreateForumTopicDTO.builder()
                .title("New Topic")
                .description("Topic description")
                .build();

        mockMvc.perform(post("/api/forum/topic")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createForumTopic_duplicate_returns409() throws Exception {
        when(forumTopicService.createTopic(any(CreateForumTopicDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ForumTopicAlreadyExistsException("Topic already exists"));

        CreateForumTopicDTO dto = CreateForumTopicDTO.builder()
                .title("Duplicate Topic")
                .description("Topic description")
                .build();

        mockMvc.perform(post("/api/forum/topic")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void createForumTopic_valid_returns200() throws Exception {
        ForumTopicDTO topicDTO = ForumTopicDTO.builder()
                .id(1L)
                .title("New Topic")
                .description("Topic description")
                .build();
        when(forumTopicService.createTopic(any(CreateForumTopicDTO.class), any(HttpServletRequest.class)))
                .thenReturn(topicDTO);

        CreateForumTopicDTO dto = CreateForumTopicDTO.builder()
                .title("New Topic")
                .description("Topic description")
                .build();

        mockMvc.perform(post("/api/forum/topic")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getForumTopicsByAuthor_returns200() throws Exception {
        when(forumTopicService.getForumTopicsByAuthor(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/forum/topic/author/1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getForumTopicById_found_returns200() throws Exception {
        ForumTopicDTO topicDTO = ForumTopicDTO.builder()
                .id(1L)
                .title("Existing Topic")
                .description("Topic description")
                .build();
        when(forumTopicService.getForumTopicById(1L)).thenReturn(topicDTO);

        mockMvc.perform(get("/api/forum/topic/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getForumTopicById_notFound_returns404() throws Exception {
        when(forumTopicService.getForumTopicById(999L))
                .thenThrow(new ForumTopicNotFoundException("Topic not found"));

        mockMvc.perform(get("/api/forum/topic/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getForumPostsByTopic_returns200() throws Exception {
        when(forumPostService.getForumPostsByTopic(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/forum/topic/1/posts")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void isTopicOwner_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/forum/topic/1/is-owner"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void isTopicOwner_authenticated_returns200() throws Exception {
        when(forumTopicService.isTopicOwner(anyLong(), any(HttpServletRequest.class)))
                .thenReturn(true);

        mockMvc.perform(get("/api/forum/topic/1/is-owner"))
                .andExpect(status().isOk());
    }

    @Test
    void closeForumTopic_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put("/api/forum/topic/1/close")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void closeForumTopic_notFound_returns404() throws Exception {
        when(forumTopicService.closeTopic(anyLong(), any(HttpServletRequest.class)))
                .thenThrow(new ForumTopicNotFoundException("Topic not found"));

        mockMvc.perform(put("/api/forum/topic/999/close")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void closeForumTopic_authenticated_returns200() throws Exception {
        when(forumTopicService.closeTopic(anyLong(), any(HttpServletRequest.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/forum/topic/1/close")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
