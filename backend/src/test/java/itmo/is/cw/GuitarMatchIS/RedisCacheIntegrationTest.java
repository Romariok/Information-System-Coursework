package itmo.is.cw.GuitarMatchIS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import itmo.is.cw.GuitarMatchIS.models.Country;
import itmo.is.cw.GuitarMatchIS.models.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Redis serializer used in production (RedisCacheConfig).
 * Previously test 6.9 (cache hit) was skipped because there was no real Redis.
 *
 * Covers the three main failure modes:
 *  1. LocalDateTime — GenericJackson2JsonRedisSerializer without JavaTimeModule
 *     throws "Cannot serialize java.time.LocalDateTime" at first cache PUT.
 *  2. Enum types — without type info, deserialized as String; cast to enum fails at cache GET.
 *  3. Lombok @Data classes — without default typing, deserialized as LinkedHashMap;
 *     cast to concrete type fails at cache GET.
 *
 * The RedisCacheConfig copies the Spring-configured ObjectMapper and adds JavaTimeModule.
 * This test verifies that setup survives a full Redis roundtrip with a real Redis instance.
 */
@Testcontainers
@SuppressWarnings("resource")
class RedisCacheIntegrationTest {

    @Container
    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    /** Mirrors the payload that cached service methods return. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    static class SampleDto {
        private Long id;
        private String name;
        private LocalDateTime createdAt;
        private Country country;
        private Genre genre;
    }

    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                REDIS.getHost(), REDIS.getMappedPort(6379));
        factory.afterPropertiesSet();

        // Mirrors RedisCacheConfig: copy base ObjectMapper, add JavaTimeModule,
        // disable timestamp serialization.  activateDefaultTyping is required so that
        // GenericJackson2JsonRedisSerializer includes @class in the JSON and can
        // reconstruct the concrete type on GET (not LinkedHashMap).
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(mapper);

        redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(valueSerializer);
        redisTemplate.afterPropertiesSet();
    }

    // =========================================================================
    // 6.9 (was skipped): LocalDateTime roundtrip via real Redis
    // Fails without JavaTimeModule: "No serializer found for LocalDateTime"
    // =========================================================================

    @Test
    void localDateTime_withJavaTimeModule_roundTripsCorrectly() {
        LocalDateTime ts = LocalDateTime.of(2024, 6, 15, 10, 30, 0);
        SampleDto dto = SampleDto.builder().id(1L).name("Test").createdAt(ts).build();

        redisTemplate.opsForValue().set("tc:ldt", dto);
        Object retrieved = redisTemplate.opsForValue().get("tc:ldt");

        assertThat(retrieved).isInstanceOf(SampleDto.class);
        assertThat(((SampleDto) retrieved).getCreatedAt()).isEqualTo(ts);
    }

    // =========================================================================
    // Enum roundtrip: Genre and Country must deserialize back to enum, not String
    // =========================================================================

    @Test
    void customEnums_roundTripAsEnumNotString() {
        SampleDto dto = SampleDto.builder()
                .id(2L)
                .country(Country.JAPAN)
                .genre(Genre.METAL)
                .build();

        redisTemplate.opsForValue().set("tc:enums", dto);
        Object retrieved = redisTemplate.opsForValue().get("tc:enums");

        assertThat(retrieved).isInstanceOf(SampleDto.class);
        SampleDto result = (SampleDto) retrieved;
        assertThat(result.getCountry()).isEqualTo(Country.JAPAN);
        assertThat(result.getGenre()).isEqualTo(Genre.METAL);
    }

    // =========================================================================
    // Cache hit simulation (test 6.9): second GET must return an equal object,
    // not LinkedHashMap (which would cause ClassCastException in the service layer).
    // =========================================================================

    @Test
    void cacheHit_secondGetReturnsConcreteDtoNotLinkedHashMap() {
        SampleDto original = SampleDto.builder()
                .id(3L)
                .name("Cached Value")
                .createdAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .country(Country.USA)
                .genre(Genre.ROCK)
                .build();

        redisTemplate.opsForValue().set("tc:hit", original);

        Object first = redisTemplate.opsForValue().get("tc:hit");
        Object second = redisTemplate.opsForValue().get("tc:hit");

        assertThat(first).isInstanceOf(SampleDto.class);
        assertThat(second).isInstanceOf(SampleDto.class);
        assertThat((SampleDto) first).isEqualTo((SampleDto) second);
        assertThat(((SampleDto) first).getName()).isEqualTo("Cached Value");
    }

    // =========================================================================
    // Null-value behavior: Redis returns null for missing keys (no NPE)
    // =========================================================================

    @Test
    void missingKey_returnsNull_doesNotThrow() {
        Object result = redisTemplate.opsForValue().get("tc:nonexistent_key_xyz");
        assertThat(result).isNull();
    }

    // =========================================================================
    // Overwrite: second PUT replaces first value correctly
    // =========================================================================

    @Test
    void overwrite_secondPutReplacesValue() {
        SampleDto v1 = SampleDto.builder().id(1L).name("First").build();
        SampleDto v2 = SampleDto.builder().id(1L).name("Second").build();

        redisTemplate.opsForValue().set("tc:overwrite", v1);
        redisTemplate.opsForValue().set("tc:overwrite", v2);

        Object result = redisTemplate.opsForValue().get("tc:overwrite");
        assertThat(result).isInstanceOf(SampleDto.class);
        assertThat(((SampleDto) result).getName()).isEqualTo("Second");
    }
}
