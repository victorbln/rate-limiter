package com.vbalan.rate_limiter.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisStorageTest {

  @Mock private RedisTemplate<String, String> redisTemplate;

  @Mock private ValueOperations<String, String> valueOperations;

  private RedisStorage storage;

  @BeforeEach
  void setUp() {
    storage = new RedisStorage(redisTemplate);
  }

  @Test
  void set_WithDuration_ShouldCallRedisWithExpiration() {
    String key = "test-key";
    String value = "test-value";
    Duration duration = Duration.ofMinutes(1);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key, value, duration);

    verify(valueOperations).set(key, value, duration);
    verify(redisTemplate).opsForValue();
  }

  @Test
  void set_WithoutDuration_ShouldCallRedisWithoutExpiration() {
    String key = "test-key";
    String value = "test-value";
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key, value, null);

    verify(valueOperations).set(key, value);
    verify(redisTemplate).opsForValue();
  }

  @Test
  void getString_ExistingKey_ShouldReturnValue() {
    String key = "test-key";
    String expectedValue = "test-value";
    when(valueOperations.get(key)).thenReturn(expectedValue);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String result = storage.getString(key);

    assertEquals(expectedValue, result);
    verify(valueOperations).get(key);
  }

  @Test
  void getString_NonExistentKey_ShouldReturnNull() {
    String key = "non-existent-key";
    when(valueOperations.get(key)).thenReturn(null);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String result = storage.getString(key);

    assertNull(result);
    verify(valueOperations).get(key);
  }

  @Test
  void get_ValidNumericValue_ShouldReturnLong() {
    String key = "numeric-key";
    String numericValue = "12345";
    when(valueOperations.get(key)).thenReturn(numericValue);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Long result = storage.get(key);

    assertEquals(12345L, result);
    verify(valueOperations).get(key);
  }

  @Test
  void get_NonExistentKey_ShouldReturnNull() {
    String key = "non-existent-key";
    when(valueOperations.get(key)).thenReturn(null);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Long result = storage.get(key);

    assertNull(result);
    verify(valueOperations).get(key);
  }

  @Test
  void get_InvalidNumericValue_ShouldThrowNumberFormatException() {
    String key = "invalid-numeric-key";
    when(valueOperations.get(key)).thenReturn("not-a-number");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    assertThrows(NumberFormatException.class, () -> storage.get(key));
    verify(valueOperations).get(key);
  }

  @Test
  void delete_ShouldCallRedisDelete() {
    String key = "delete-key";

    storage.delete(key);

    verify(redisTemplate).delete(key);
  }

  @Test
  void expire_ShouldCallRedisExpire() {
    String key = "expire-key";
    Duration duration = Duration.ofMinutes(5);

    storage.expire(key, duration);

    verify(redisTemplate).expire(key, duration);
  }

  @Test
  void set_ZeroDuration_ShouldSetWithoutExpiration() {
    String key = "zero-duration-key";
    String value = "test-value";
    Duration zeroDuration = Duration.ZERO;
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key, value, zeroDuration);

    verify(valueOperations).set(key, value, zeroDuration);
  }

  @Test
  void set_NegativeDuration_ShouldSetWithNegativeDuration() {
    String key = "negative-duration-key";
    String value = "test-value";
    Duration negativeDuration = Duration.ofMinutes(-1);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key, value, negativeDuration);

    verify(valueOperations).set(key, value, negativeDuration);
  }

  @Test
  void getString_EmptyString_ShouldReturnEmptyString() {
    String key = "empty-key";
    when(valueOperations.get(key)).thenReturn("");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    String result = storage.getString(key);

    assertEquals("", result);
    verify(valueOperations).get(key);
  }

  @Test
  void get_EmptyString_ShouldThrowNumberFormatException() {
    String key = "empty-numeric-key";
    when(valueOperations.get(key)).thenReturn("");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    assertThrows(NumberFormatException.class, () -> storage.get(key));
    verify(valueOperations).get(key);
  }

  @Test
  void get_ZeroValue_ShouldReturnZero() {
    String key = "zero-key";
    when(valueOperations.get(key)).thenReturn("0");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Long result = storage.get(key);

    assertEquals(0L, result);
    verify(valueOperations).get(key);
  }

  @Test
  void get_NegativeValue_ShouldReturnNegativeNumber() {
    String key = "negative-key";
    when(valueOperations.get(key)).thenReturn("-123");
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    Long result = storage.get(key);

    assertEquals(-123L, result);
    verify(valueOperations).get(key);
  }

  @Test
  void multipleOperations_ShouldCallRedisMultipleTimes() {
    String key1 = "key1";
    String key2 = "key2";
    String value1 = "value1";
    String value2 = "value2";
    Duration duration = Duration.ofMinutes(1);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key1, value1, duration);
    storage.set(key2, value2, null);
    storage.getString(key1);
    storage.delete(key2);

    verify(valueOperations).set(key1, value1, duration);
    verify(valueOperations).set(key2, value2);
    verify(valueOperations).get(key1);
    verify(redisTemplate).delete(key2);
    verify(redisTemplate, times(3)).opsForValue();
  }

  @Test
  void redisTemplate_ShouldBeCalledWithCorrectParameters() {
    String key = "parameter-test";
    String value = "parameter-value";
    Duration duration = Duration.ofSeconds(30);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    storage.set(key, value, duration);
    storage.getString(key);
    storage.expire(key, duration);
    storage.delete(key);

    verify(valueOperations).set(key, value, duration);
    verify(valueOperations).get(key);
    verify(redisTemplate).expire(key, duration);
    verify(redisTemplate).delete(key);
  }
}
