package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.CreateForumPostDTO;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ForumPostService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForumPostController.class)
@Import(TestWebMvcSecurityConfig.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ForumPostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ForumPostService forumPostService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void createForumPost_closedTopic_returns403() throws Exception {
        when(forumPostService.createForumPost(any(CreateForumPostDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ForbiddenException("Topic is closed"));

        CreateForumPostDTO dto = CreateForumPostDTO.builder()
                .forumTopicId(2L)
                .content("My post content")
                .build();

        mockMvc.perform(post("/api/forum/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createForumPost_unauthenticated_returns401() throws Exception {
        CreateForumPostDTO dto = CreateForumPostDTO.builder()
                .forumTopicId(1L)
                .content("My post content")
                .build();

        mockMvc.perform(post("/api/forum/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
