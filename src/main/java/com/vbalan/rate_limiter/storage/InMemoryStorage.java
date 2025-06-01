package com.vbalan.rate_limiter.storage;

import com.vbalan.rate_limiter.exception.StorageInitializationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InMemoryStorage implements RateLimitStorage {
  private final ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LocalDateTime> expirations = new ConcurrentHashMap<>();

  public InMemoryStorage() {
    try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
      scheduler.scheduleAtFixedRate(this::cleanupExpiredKeys, 1, 1, TimeUnit.MINUTES);
    } catch (Exception e) {
      throw new StorageInitializationException("Unable to initialize in memory storage");
    }
  }

  @Override
  public void expire(String key, Duration duration) {
    expirations.put(key, LocalDateTime.now().plus(duration));
  }

  @Override
  public Long get(String key) {
    if (isExpired(key)) {
      delete(key);
      return null;
    }
    String value = storage.get(key);
    return value != null ? Long.parseLong(value) : null;
  }

  @Override
  public void set(String key, String value, Duration duration) {
    storage.put(key, value);
    if (duration != null) {
      expire(key, duration);
    }
  }

  @Override
  public void delete(String key) {
    storage.remove(key);
    expirations.remove(key);
  }

  @Override
  public String getString(String key) {
    if (isExpired(key)) {
      delete(key);
      return null;
    }
    return storage.get(key);
  }

  private boolean isExpired(String key) {
    LocalDateTime expiration = expirations.get(key);
    return expiration != null && LocalDateTime.now().isAfter(expiration);
  }

  private void cleanupExpiredKeys() {
    LocalDateTime now = LocalDateTime.now();
    expirations
        .entrySet()
        .removeIf(
            entry -> {
              if (now.isAfter(entry.getValue())) {
                storage.remove(entry.getKey());
                return true;
              }
              return false;
            });
  }
}
