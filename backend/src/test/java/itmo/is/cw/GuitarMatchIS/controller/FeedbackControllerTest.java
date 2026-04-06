package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.CreateProductFeedbackDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.FeedbackService;
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

@WebMvcTest(FeedbackController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class FeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FeedbackService feedbackService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    void addProductFeedback_unauthenticated_returns401() throws Exception {
        CreateProductFeedbackDTO dto = new CreateProductFeedbackDTO(1L, "Great product", 5);

        mockMvc.perform(post("/api/feedback/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void addProductFeedback_starsTooBig_returns400() throws Exception {
        mockMvc.perform(post("/api/feedback/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"text\":\"Great product\",\"stars\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void addProductFeedback_starsTooSmall_returns400() throws Exception {
        mockMvc.perform(post("/api/feedback/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":1,\"text\":\"Great product\",\"stars\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void addProductFeedback_valid_returns200() throws Exception {
        when(feedbackService.addProductFeedback(any(CreateProductFeedbackDTO.class), any(HttpServletRequest.class)))
                .thenReturn(true);

        CreateProductFeedbackDTO dto = new CreateProductFeedbackDTO(1L, "Great product", 5);

        mockMvc.perform(post("/api/feedback/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getFeedbackByProductId_noFeedback_returns200EmptyList() throws Exception {
        when(feedbackService.getFeedbackByProductId(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/feedback/product/1")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
