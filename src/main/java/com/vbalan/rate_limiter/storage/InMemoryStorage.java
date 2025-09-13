package com.vbalan.rate_limiter.storage;

import com.vbalan.rate_limiter.exception.StorageInitializationException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * In-memory implementation of the rate limit storage interface.
 * This implementation uses concurrent hash maps for thread-safe storage
 * and includes automatic cleanup of expired keys.
 */
@Slf4j
public class InMemoryStorage implements RateLimitStorage {
  private final ConcurrentHashMap<String, String> storage = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, LocalDateTime> expirations = new ConcurrentHashMap<>();
  private final ScheduledExecutorService scheduler;

  /**
   * Initializes the in-memory storage with automatic cleanup.
   * 
   * @throws StorageInitializationException if initialization fails
   */
  public InMemoryStorage() {
    try {
      this.scheduler = Executors.newScheduledThreadPool(1);
      scheduler.scheduleAtFixedRate(this::cleanupExpiredKeys, 1, 1, TimeUnit.MINUTES);
      log.info("In-memory storage initialized with cleanup scheduler");
    } catch (Exception e) {
      log.error("Failed to initialize in-memory storage", e);
      throw new StorageInitializationException("Unable to initialize in memory storage");
    }
  }

  @Override
  public void expire(String key, Duration duration) {
    expirations.put(key, LocalDateTime.now().plus(duration));
    log.debug("Set expiration for key: {} in {}", key, duration);
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
    log.debug("Stored key: {} with value: {}", key, value);
  }

  @Override
  public void delete(String key) {
    storage.remove(key);
    expirations.remove(key);
    log.debug("Deleted key: {}", key);
  }

  @Override
  public String getString(String key) {
    if (isExpired(key)) {
      delete(key);
      return null;
    }
    return storage.get(key);
  }

  /**
   * Checks if a key has expired.
   * 
   * @param key the key to check
   * @return true if the key has expired, false otherwise
   */
  private boolean isExpired(String key) {
    LocalDateTime expiration = expirations.get(key);
    return expiration != null && LocalDateTime.now().isAfter(expiration);
  }

  /**
   * Cleanup task that removes expired keys from both storage maps.
   * This method is called periodically by the scheduled executor.
   */
  private void cleanupExpiredKeys() {
    LocalDateTime now = LocalDateTime.now();
    int cleanedCount = 0;
    
    var iterator = expirations.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      if (now.isAfter(entry.getValue())) {
        storage.remove(entry.getKey());
        iterator.remove();
        cleanedCount++;
      }
    }
    
    if (cleanedCount > 0) {
      log.debug("Cleaned up {} expired keys", cleanedCount);
    }
  }

  /**
   * Shutdown the cleanup scheduler.
   * Should be called when the storage is no longer needed.
   */
  public void shutdown() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
        log.info("In-memory storage cleanup scheduler shutdown completed");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        scheduler.shutdownNow();
        log.warn("Cleanup scheduler shutdown interrupted", e);
      }
    }
  }
}
