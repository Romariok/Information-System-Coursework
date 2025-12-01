package itmo.is.cw.GuitarMatchIS.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisCacheConfig {

   @Bean
   public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper,
         CacheProperties cacheProperties) {
      ObjectMapper mapper = objectMapper.copy();
      mapper.registerModule(new JavaTimeModule());
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

      RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                  RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                  .fromSerializer(new GenericJackson2JsonRedisSerializer(mapper)));

      CacheProperties.Redis redisProps = cacheProperties.getRedis();
      if (redisProps.getTimeToLive() != null) {
         config = config.entryTtl(redisProps.getTimeToLive());
      }
      if (redisProps.getKeyPrefix() != null) {
         config = config.prefixCacheNameWith(redisProps.getKeyPrefix());
      }
      if (!redisProps.isCacheNullValues()) {
         config = config.disableCachingNullValues();
      }
      if (!redisProps.isUseKeyPrefix()) {
         config = config.disableKeyPrefix();
      }
      return config;
   }
}

