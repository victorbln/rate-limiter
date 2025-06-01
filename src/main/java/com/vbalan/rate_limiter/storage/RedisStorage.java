package com.vbalan.rate_limiter.storage;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

@RequiredArgsConstructor
public class RedisStorage implements RateLimitStorage {
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void expire(String key, Duration duration) {
    redisTemplate.expire(key, duration);
  }

  @Override
  public Long get(String key) {
    String value = redisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(value) : null;
  }

  @Override
  public void set(String key, String value, Duration duration) {
    if (duration != null) {
      redisTemplate.opsForValue().set(key, value, duration);
    } else {
      redisTemplate.opsForValue().set(key, value);
    }
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }
}
