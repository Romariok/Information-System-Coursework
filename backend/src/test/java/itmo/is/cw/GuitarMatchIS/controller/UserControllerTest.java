package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.AddUserProductDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.UserService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductUserNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
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

@WebMvcTest(UserController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getRoleByUsername_notFound_returns404() throws Exception {
        when(userService.getRoleByUsername("nonexistent"))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/role/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProductToUser_unauthenticated_returns401() throws Exception {
        AddUserProductDTO dto = new AddUserProductDTO(1L);

        mockMvc.perform(post("/api/user/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deleteProductFromUser_notFound_returns404() throws Exception {
        when(userService.deleteProductFromUser(any(AddUserProductDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ProductUserNotFoundException("Product not found for user"));

        AddUserProductDTO dto = new AddUserProductDTO(999L);

        mockMvc.perform(delete("/api/user/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserProducts_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/user/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getSubscribedMusicians_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/user/subscribed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getSubscribedMusicians_authenticated_returns200() throws Exception {
        when(userService.getSubscribedMusicians(any(HttpServletRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user/subscribed"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json("[]"));
    }
}
