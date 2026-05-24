package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.AuthResponseDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.AuthService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserAlreadyExistException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void register_valid_returns200WithToken() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO(
                "newuser", false, 0, LocalDateTime.now(), "generated-token");
        when(authService.register(any(UserDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDTO("newuser", "pass12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("generated-token"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void register_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"pass12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void register_duplicate_returns409() throws Exception {
        when(authService.register(any(UserDTO.class)))
                .thenThrow(new UserAlreadyExistException("Username testuser already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDTO("testuser", "pass12345"))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void login_valid_returns200() throws Exception {
        AuthResponseDTO response = new AuthResponseDTO(
                "testuser", false, 0, LocalDateTime.now(), "login-token");
        when(authService.login(any(UserDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDTO("testuser", "pass12345"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("login-token"));
    }

    @Test
    @WithMockUser
    void login_badPassword_returns401() throws Exception {
        when(authService.login(any(UserDTO.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDTO("testuser", "wrongpass"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void login_missingUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"pass12345\"}"))
                .andExpect(status().isBadRequest());
    }
}
