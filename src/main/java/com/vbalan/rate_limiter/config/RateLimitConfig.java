package com.vbalan.rate_limiter.config;

import com.vbalan.rate_limiter.storage.InMemoryStorage;
import com.vbalan.rate_limiter.storage.RateLimitStorage;
import com.vbalan.rate_limiter.storage.RedisStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RateLimitConfig {
  @Value("${rate-limit.storage.type:memory}")
  private String storageType;

  @Bean
  public RateLimitStorage rateLimitStorage(RedisTemplate<String, String> redisTemplate) {
    if (storageType.equalsIgnoreCase("redis")) {
      return new RedisStorage(redisTemplate);
    }
    return new InMemoryStorage();
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    return template;
  }
}
