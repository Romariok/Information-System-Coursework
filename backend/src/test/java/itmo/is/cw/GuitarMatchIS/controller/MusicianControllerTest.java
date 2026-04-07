package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.dto.AddProductMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.CreateMusicianDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianInfoDTO;
import itmo.is.cw.GuitarMatchIS.dto.MusicianProductDTO;
import itmo.is.cw.GuitarMatchIS.dto.SubscribeDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.MusicianService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductMusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionNotFoundException;
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

@WebMvcTest(MusicianController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class MusicianControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MusicianService musicianService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getMusicians_missingSortBy_returns400() throws Exception {
        mockMvc.perform(get("/api/musician")
                        .param("ascending", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getMusicians_withParams_returns200() throws Exception {
        when(musicianService.getMusician(anyInt(), anyInt(), any(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/musician")
                        .param("sortBy", "NAME")
                        .param("ascending", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMusicianInfo_notFound_returns404() throws Exception {
        when(musicianService.getMusicianInfo(99L))
                .thenThrow(new MusicianNotFoundException("Musician not found"));

        mockMvc.perform(get("/api/musician/id/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createMusician_unauthenticated_returns401() throws Exception {
        CreateMusicianDTO dto = new CreateMusicianDTO("Test Band", Collections.emptyList(), Collections.emptyList());

        mockMvc.perform(post("/api/musician")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createMusician_valid_returns200() throws Exception {
        MusicianInfoDTO info = MusicianInfoDTO.builder()
                .id(1L)
                .name("Test Band")
                .subscribers(0)
                .genres(Collections.emptyList())
                .typesOfMusicians(Collections.emptyList())
                .products(Collections.emptyList())
                .build();
        when(musicianService.createMusician(any(CreateMusicianDTO.class), any(HttpServletRequest.class)))
                .thenReturn(info);

        CreateMusicianDTO dto = new CreateMusicianDTO("Test Band", Collections.emptyList(), Collections.emptyList());

        mockMvc.perform(post("/api/musician")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void createMusician_duplicate_returns409() throws Exception {
        when(musicianService.createMusician(any(CreateMusicianDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new MusicianAlreadyExistsException("Musician already exists"));

        CreateMusicianDTO dto = new CreateMusicianDTO("Test Band", Collections.emptyList(), Collections.emptyList());

        mockMvc.perform(post("/api/musician")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void subscribeToMusician_unauthenticated_returns401() throws Exception {
        SubscribeDTO dto = new SubscribeDTO(1L);

        mockMvc.perform(post("/api/musician/subscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void subscribeToMusician_alreadySubscribed_returns409() throws Exception {
        when(musicianService.subscribeToMusician(any(SubscribeDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new SubscriptionAlreadyExistsException("Already subscribed"));

        SubscribeDTO dto = new SubscribeDTO(1L);

        mockMvc.perform(post("/api/musician/subscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void unsubscribeFromMusician_unauthenticated_returns401() throws Exception {
        SubscribeDTO dto = new SubscribeDTO(1L);

        mockMvc.perform(delete("/api/musician/subscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void unsubscribeFromMusician_notFound_returns404() throws Exception {
        when(musicianService.unsubscribeFromMusician(any(SubscribeDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new SubscriptionNotFoundException("Subscription not found"));

        SubscribeDTO dto = new SubscribeDTO(1L);

        mockMvc.perform(delete("/api/musician/subscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addProductToMusician_unauthenticated_returns401() throws Exception {
        AddProductMusicianDTO dto = new AddProductMusicianDTO("Test Band", 1L);

        mockMvc.perform(post("/api/musician/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void deleteProductFromMusician_notFound_returns404() throws Exception {
        when(musicianService.deleteProductFromMusician(any(AddProductMusicianDTO.class), any(HttpServletRequest.class)))
                .thenThrow(new ProductMusicianNotFoundException("Link not found"));

        AddProductMusicianDTO dto = new AddProductMusicianDTO("Test Band", 999L);

        mockMvc.perform(delete("/api/musician/product")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getMusicianProducts_withUnderscores_returns200() throws Exception {
        MusicianProductDTO result = MusicianProductDTO.builder()
                .products(Collections.emptyList())
                .build();
        when(musicianService.getMusicianProducts("Test Band"))
                .thenReturn(result);

        mockMvc.perform(get("/api/musician/Test_Band/products"))
                .andExpect(status().isOk());
    }
}
