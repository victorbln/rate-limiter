package com.vbalan.rate_limiter.storage;

import java.time.Duration;

public interface RateLimitStorage {
  void expire(String key, Duration duration);

  Long get(String key);

  void set(String key, String value, Duration duration);

  void delete(String key);

  String getString(String key);
}
