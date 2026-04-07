package itmo.is.cw.GuitarMatchIS.controller;

import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import itmo.is.cw.GuitarMatchIS.security.service.AuthUserDetailsService;
import itmo.is.cw.GuitarMatchIS.service.ProductService;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ArticleNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ForbiddenException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.MusicianNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.ProductNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionAlreadyExistsException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.SubscriptionNotFoundException;
import itmo.is.cw.GuitarMatchIS.utils.exceptions.UserAlreadyExistException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ProductController.class)
@Import(JwtUtils.class)
@TestPropertySource(properties = {
        "spring.cache.type=none",
        "app.security.jwt.secret=test-secret-key-for-controller-tests-1234"
})
class ExceptionHandlerMvcTest {

    private static final String FILTER_URL =
            "/api/product/filter?sortBy=NAME&ascending=true&from=0&size=10";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private AuthUserDetailsService userDetailsService;

    // =========================================================================
    // PostgreSQL недоступен
    // =========================================================================

    /**
     * 6.2: productRepository (через сервис) бросает CannotGetJdbcConnectionException →
     * GlobalControllerExceptionHandler перехватывает DataAccessException → 500 с JSON ErrorResponse.
     */
    @Test
    @WithMockUser
    void productService_cannotGetJdbcConnection_returns500() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new CannotGetJdbcConnectionException("PostgreSQL unavailable"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * 6.5: сервис бросает QueryTimeoutException (DataAccessException) →
     * ответ 500, не 401 (задокументировано как gap G6).
     */
    @Test
    @WithMockUser
    void productService_queryTimeout_returns500NotUnauthorized() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new QueryTimeoutException("DB query timed out"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isInternalServerError());
    }

    /**
     * DataAccessResourceFailureException (подкласс DataAccessException) → 500.
     */
    @Test
    @WithMockUser
    void productService_dataAccessResourceFailure_returns500() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new DataAccessResourceFailureException("Connection pool exhausted"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isInternalServerError());
    }

    // =========================================================================
    // Section H — GlobalControllerExceptionHandler корректные коды
    // =========================================================================

    /**
     * 6.33: ForbiddenException → 403.
     */
    @Test
    @WithMockUser
    void forbiddenException_returns403() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new ForbiddenException("Access denied"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isForbidden());
    }

    /**
     * 6.34: MusicianNotFoundException → 404.
     */
    @Test
    @WithMockUser
    void musicianNotFoundException_returns404() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new MusicianNotFoundException("Musician not found"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isNotFound());
    }

    /**
     * 6.35: ArticleNotFoundException → 404.
     */
    @Test
    @WithMockUser
    void articleNotFoundException_returns404() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new ArticleNotFoundException("Article not found"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isNotFound());
    }

    /**
     * 6.36: ProductNotFoundException → 404.
     */
    @Test
    @WithMockUser
    void productNotFoundException_returns404() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new ProductNotFoundException("Product not found"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isNotFound());
    }

    /**
     * 6.37: UserAlreadyExistException → 409.
     */
    @Test
    @WithMockUser
    void userAlreadyExistException_returns409() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new UserAlreadyExistException("User already exists"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isConflict());
    }

    /**
     * 6.38: MusicianAlreadyExistsException → 409.
     */
    @Test
    @WithMockUser
    void musicianAlreadyExistsException_returns409() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new MusicianAlreadyExistsException("Musician already exists"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isConflict());
    }

    /**
     * 6.39: SubscriptionAlreadyExistsException → 409.
     */
    @Test
    @WithMockUser
    void subscriptionAlreadyExistsException_returns409() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new SubscriptionAlreadyExistsException("Already subscribed"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isConflict());
    }

    /**
     * 6.40: SubscriptionNotFoundException → 404.
     */
    @Test
    @WithMockUser
    void subscriptionNotFoundException_returns404() throws Exception {
        when(productService.getProductsByFilter(any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                anyBoolean(), anyInt(), anyInt()))
                .thenThrow(new SubscriptionNotFoundException("Subscription not found"));

        mockMvc.perform(get(FILTER_URL))
                .andExpect(status().isNotFound());
    }
}
