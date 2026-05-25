package itmo.is.cw.GuitarMatchIS.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {

   @Bean
   public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.registerModule(new JavaTimeModule());
      mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
      mapper.activateDefaultTyping(
            mapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL);

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

   @Override
   public CacheErrorHandler errorHandler() {
      return new CacheErrorHandler() {
         @Override
         public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache GET error on cache '{}' for key '{}': {}", cache.getName(), key, exception.getMessage());
         }

         @Override
         public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
            log.warn("Cache PUT error on cache '{}' for key '{}': {}", cache.getName(), key, exception.getMessage());
         }

         @Override
         public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
            log.warn("Cache EVICT error on cache '{}' for key '{}': {}", cache.getName(), key, exception.getMessage());
         }

         @Override
         public void handleCacheClearError(RuntimeException exception, Cache cache) {
            log.warn("Cache CLEAR error on cache '{}': {}", cache.getName(), exception.getMessage());
         }
      };
   }
}
