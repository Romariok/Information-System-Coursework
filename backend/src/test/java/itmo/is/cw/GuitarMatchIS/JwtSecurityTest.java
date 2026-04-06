package itmo.is.cw.GuitarMatchIS;

import itmo.is.cw.GuitarMatchIS.security.jwt.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Section E — JWT граничные случаи (6.20–6.25).
 * Тестирует JwtUtils напрямую без Spring-контекста.
 */
class JwtSecurityTest {

    private JwtUtils jwtUtils;
    private static final String TEST_SECRET = "test-secret-key-for-jwt-security-tests-1234";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", 3_600_000L);
    }

    /**
     * 6.20: Запрос без Authorization header → parseJwt возвращает null
     * (JwtAuthTokenFilter пропускает токен → Spring Security считает запрос анонимным → 401).
     */
    @Test
    void parseJwt_noAuthorizationHeader_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        assertThat(jwtUtils.parseJwt(request)).isNull();
    }

    /**
     * 6.21: Bearer <malformed_token> → validateJwtToken возвращает false.
     */
    @Test
    void validateJwtToken_malformedToken_returnsFalse() {
        assertThat(jwtUtils.validateJwtToken("not.a.valid.jwt.token")).isFalse();
    }

    /**
     * 6.22: Bearer <expired_token> → validateJwtToken возвращает false.
     * Для создания истёкшего токена expirationMs устанавливается в -1.
     */
    @Test
    void validateJwtToken_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtils, "expirationMs", -1L);
        String expiredToken = jwtUtils.generateJwtToken("testuser");

        assertThat(jwtUtils.validateJwtToken(expiredToken)).isFalse();
    }

    /**
     * 6.23: Валидный токен, но пользователь удалён из БД → UsernameNotFoundException →
     * 401. Тестируется на уровне JwtUtils: getUserNameFromJwtToken должен вернуть username
     * из токена, а обработка отсутствующего пользователя — в слое фильтра/сервиса.
     */
    @Test
    void validateJwtToken_validToken_returnsTrue() {
        String token = jwtUtils.generateJwtToken("deleteduser");

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("deleteduser");
    }

    /**
     * 6.24: Токен с испорченной подписью → validateJwtToken возвращает false.
     */
    @Test
    void validateJwtToken_corruptedSignature_returnsFalse() {
        String validToken = jwtUtils.generateJwtToken("testuser");
        String corruptedToken = validToken + "CORRUPTED";

        assertThat(jwtUtils.validateJwtToken(corruptedToken)).isFalse();
    }

    /**
     * 6.25: Authorization заголовок без префикса "Bearer " →
     * parseJwt возвращает null.
     */
    @Test
    void parseJwt_tokenWithoutBearerPrefix_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String tokenWithoutPrefix = jwtUtils.generateJwtToken("testuser");
        when(request.getHeader("Authorization")).thenReturn(tokenWithoutPrefix);

        assertThat(jwtUtils.parseJwt(request)).isNull();
    }

    /**
     * 6.25 (дополнительно): Authorization: "Token <value>" (не "Bearer") →
     * parseJwt возвращает null.
     */
    @Test
    void parseJwt_wrongAuthorizationScheme_returnsNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Token some-token-value");

        assertThat(jwtUtils.parseJwt(request)).isNull();
    }

    /**
     * Верный формат: "Bearer <token>" → parseJwt возвращает сам токен.
     */
    @Test
    void parseJwt_validBearerToken_returnsToken() {
        String token = jwtUtils.generateJwtToken("testuser");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        assertThat(jwtUtils.parseJwt(request)).isEqualTo(token);
    }

    /**
     * Полный цикл: generate → validate → getUserName.
     */
    @Test
    void generateAndValidate_roundTrip_works() {
        String token = jwtUtils.generateJwtToken("myuser");

        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("myuser");
    }
}
