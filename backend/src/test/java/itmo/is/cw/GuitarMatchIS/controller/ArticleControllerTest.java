package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.ArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateArticleDTO;
import itmo.is.cw.GuitarMatchIS.dto.ModerateArticleDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ArticleService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArticleController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getAcceptedArticles_missingParams_returns400() throws Exception {
        mockMvc.perform(get("/api/article")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getAcceptedArticles_withParams_returns200() throws Exception {
        when(articleService.getAcceptedArticles(anyInt(), anyInt(), any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/article")
                        .param("sortBy", "CREATED_AT")
                        .param("ascending", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getUnacceptedArticles_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/article/unaccepted")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "regularuser")
    void getUnacceptedArticles_notAdmin_returns403() throws Exception {
        when(articleService.getStatusArticles(anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenThrow(new ForbiddenException("Not an admin"));

        mockMvc.perform(get("/api/article/unaccepted")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void getUnacceptedArticles_admin_returns200() throws Exception {
        when(articleService.getStatusArticles(anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/article/unaccepted")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void createArticle_unauthenticated_returns401() throws Exception {
        CreateArticleDTO dto = new CreateArticleDTO("Gibson Les Paul", "My Article", "Some article content here.");

        mockMvc.perform(post("/api/article")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createArticle_valid_returns200() throws Exception {
        ArticleDTO articleDTO = ArticleDTO.builder()
                .id(1L)
                .header("My Article")
                .text("Some article content here.")
                .htmlContent("<p>Some article content here.</p>")
                .accepted(false)
                .build();
        when(articleService.createArticle(any(CreateArticleDTO.class), any(HttpServletRequest.class)))
                .thenReturn(articleDTO);

        CreateArticleDTO dto = new CreateArticleDTO("Gibson Les Paul", "My Article", "Some article content here.");

        mockMvc.perform(post("/api/article")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createArticle_blankHeader_returns400() throws Exception {
        mockMvc.perform(post("/api/article")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productName\":\"Gibson Les Paul\",\"header\":\"\",\"text\":\"Some text content.\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "regularuser")
    void moderateArticle_notAdmin_returns403() throws Exception {
        when(articleService.moderateArticle(any(ModerateArticleDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ForbiddenException("Not an admin"));

        ModerateArticleDTO dto = ModerateArticleDTO.builder().articleId(1L).accepted(true).build();

        mockMvc.perform(post("/api/article/moderate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN")
    void moderateArticle_articleNotFound_returns404() throws Exception {
        when(articleService.moderateArticle(any(ModerateArticleDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ArticleNotFoundException("Article not found"));

        ModerateArticleDTO dto = ModerateArticleDTO.builder().articleId(999L).accepted(true).build();

        mockMvc.perform(post("/api/article/moderate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
