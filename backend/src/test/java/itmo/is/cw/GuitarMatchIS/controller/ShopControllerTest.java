package itmo.is.cw.GuitarMatchIS.controller;

import itmo.is.cw.GuitarMatchIS.dto.ShopDTO;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ShopService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ShopNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ShopControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShopService shopService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getShops_withParams_returns200() throws Exception {
        when(shopService.getShops(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/shop")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getShopProducts_notFound_returns404() throws Exception {
        when(shopService.getShopProducts(anyLong(), anyInt(), anyInt()))
                .thenThrow(new ShopNotFoundException("Shop not found"));

        mockMvc.perform(get("/api/shop/999/products")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getShopById_found_returns200() throws Exception {
        ShopDTO shopDTO = ShopDTO.builder()
                .id(1L)
                .name("Guitar Store")
                .description("Best guitars in town")
                .website("https://example.com")
                .email("shop@example.com")
                .address("123 Main St")
                .build();
        when(shopService.getShopById(1L)).thenReturn(shopDTO);

        mockMvc.perform(get("/api/shop/id/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getShopById_notFound_returns404() throws Exception {
        when(shopService.getShopById(999L))
                .thenThrow(new ShopNotFoundException("Shop not found"));

        mockMvc.perform(get("/api/shop/id/999"))
                .andExpect(status().isNotFound());
    }
}
