package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.AddUserProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserGenreDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.UserTypeOfMusicianDTO;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import itmo.is.cw.GuitarMatchIS.models.TypeOfMusician;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.UserService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductUserNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserNotFoundException;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestWebMvcSecurityConfig.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Test
    @WithMockUser
    void getUserInfoById_found_returns200() throws Exception {
        UserInfoDTO userInfo = UserInfoDTO.builder()
                .id(1L)
                .username("testuser")
                .build();
        when(userService.getUserInfoById(1L)).thenReturn(userInfo);

        mockMvc.perform(get("/api/user/id/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserInfoById_notFound_returns404() throws Exception {
        when(userService.getUserInfoById(999L))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/user/id/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGenresByUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/user/genres"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getGenresByUser_authenticated_returns200() throws Exception {
        when(userService.getGenresByUser(any(HttpServletRequest.class)))
                .thenReturn(List.of(Genre.ROCK, Genre.BLUES));

        mockMvc.perform(get("/api/user/genres"))
                .andExpect(status().isOk());
    }

    @Test
    void getTypesOfMusiciansByUser_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/user/types"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getTypesOfMusiciansByUser_authenticated_returns200() throws Exception {
        when(userService.getTypesOfMusiciansByUser(any(HttpServletRequest.class)))
                .thenReturn(List.of(TypeOfMusician.GUITARIST));

        mockMvc.perform(get("/api/user/types"))
                .andExpect(status().isOk());
    }

    @Test
    void setGenresToUser_unauthenticated_returns401() throws Exception {
        UserGenreDTO dto = UserGenreDTO.builder().genres(List.of(Genre.ROCK)).build();

        mockMvc.perform(post("/api/user/genres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void setGenresToUser_authenticated_returns200() throws Exception {
        when(userService.setGenresToUser(any(HttpServletRequest.class), anyList()))
                .thenReturn(true);

        UserGenreDTO dto = UserGenreDTO.builder().genres(List.of(Genre.ROCK)).build();

        mockMvc.perform(post("/api/user/genres")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void setTypesOfMusiciansToUser_unauthenticated_returns401() throws Exception {
        UserTypeOfMusicianDTO dto = UserTypeOfMusicianDTO.builder()
                .typesOfMusicians(List.of(TypeOfMusician.GUITARIST)).build();

        mockMvc.perform(post("/api/user/types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void setTypesOfMusiciansToUser_authenticated_returns200() throws Exception {
        when(userService.setTypesOfMusiciansToUser(any(HttpServletRequest.class), anyList()))
                .thenReturn(true);

        UserTypeOfMusicianDTO dto = UserTypeOfMusicianDTO.builder()
                .typesOfMusicians(List.of(TypeOfMusician.GUITARIST)).build();

        mockMvc.perform(post("/api/user/types")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserProducts_authenticated_returns200() throws Exception {
        when(userService.getUserProducts(any(HttpServletRequest.class)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/user/products"))
                .andExpect(status().isOk());
    }

    @Test
    void getRoleByUsername_permitAll_unauthenticated_returns200() throws Exception {
        when(userService.getRoleByUsername("someuser"))
                .thenReturn(itmo.is.cw.GuitarMatchIS.models.Role.USER);

        mockMvc.perform(get("/api/user/role/someuser"))
                .andExpect(status().isOk());
    }
}
