package itmo.is.cw.GuitarMatchIS.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import itmo.is.cw.GuitarMatchIS.models.TypeOfProduct;
import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ProductService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ProductController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void getProductsByFilter_missingRequiredParams_returns400() throws Exception {
        mockMvc.perform(get("/api/product/filter")
                        .param("from", "0")
                        .param("size", "10")
                        .param("ascending", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getProductsByFilter_allParams_returns200() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/product/filter")
                        .param("sortBy", "NAME")
                        .param("ascending", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProductsByFilter_invalidTypeOfProduct_returns400() throws Exception {
        mockMvc.perform(get("/api/product/filter")
                        .param("typeOfProduct", "INVALID_ENUM")
                        .param("sortBy", "NAME")
                        .param("ascending", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getProductsByType_valid_returns200() throws Exception {
        when(productService.getProductsByTypeOfProduct(any(TypeOfProduct.class), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/product/type/ELECTRIC_GUITAR")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProductsByName_withSpaces_returns200() throws Exception {
        when(productService.getProductsByNameContains(anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/product/Gibson Les Paul")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProductsById_notFound_returns404() throws Exception {
        when(productService.getProductsById(99L))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get("/api/product/id/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getMusiciansByProductId_returns200() throws Exception {
        when(productService.getMusiciansByProductId(anyLong(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/product/1/musicians")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProductArticles_returns200() throws Exception {
        when(productService.getProductArticles(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);

        mockMvc.perform(get("/api/product/1/articles")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getProductShops_returns200() throws Exception {
        when(productService.getProductShops(anyLong(), anyInt(), anyInt()))
                .thenReturn(null);

        mockMvc.perform(get("/api/product/1/shops")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
