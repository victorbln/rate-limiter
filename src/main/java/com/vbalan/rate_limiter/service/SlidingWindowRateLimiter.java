package com.vbalan.rate_limiter.service;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.storage.RateLimitStorage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SlidingWindowRateLimiter {
  private final RateLimitStorage storage;
  private static final long WINDOW_SIZE_MS = 60000;

  public SlidingWindowRateLimiter(RateLimitStorage storage) {
    this.storage = storage;
  }

  public boolean allowRequest(String clientId, ClientConfiguration config) {
    long now = System.currentTimeMillis();
    String requestsKey = "sliding_requests:" + clientId;

    List<Long> requestTimestamps = getRequestTimestamps(requestsKey);

    long windowStart = now - WINDOW_SIZE_MS;
    requestTimestamps.removeIf(timestamp -> timestamp <= windowStart);

    if (requestTimestamps.size() < config.getRequestsPerMinute()) {
      requestTimestamps.add(now);

      storeRequestTimestamps(requestsKey, requestTimestamps);

      return true;
    } else {
      storeRequestTimestamps(requestsKey, requestTimestamps);
      return false;
    }
  }

  private List<Long> getRequestTimestamps(String key) {
    String timestampsStr = storage.getString(key);
    List<Long> timestamps = new ArrayList<>();

    if (timestampsStr != null && !timestampsStr.isEmpty()) {
      String[] parts = timestampsStr.split(",");
      for (String part : parts) {
        try {
          timestamps.add(Long.parseLong(part.trim()));
        } catch (NumberFormatException ignored) {
          // Ignoring invalid timestamps
        }
      }
    }

    return timestamps;
  }

  private void storeRequestTimestamps(String key, List<Long> timestamps) {
    if (timestamps.isEmpty()) {
      storage.delete(key);
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < timestamps.size(); i++) {
      if (i > 0) sb.append(",");
      sb.append(timestamps.get(i));
    }

    storage.set(key, sb.toString(), Duration.ofMinutes(2));
  }
}
