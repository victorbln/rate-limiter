package com.vbalan.rate_limiter.service;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.storage.RateLimitStorage;
import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class TokenBucketRateLimiter {
  private final RateLimitStorage storage;

  public TokenBucketRateLimiter(RateLimitStorage storage) {
    this.storage = storage;
  }

  public boolean allowRequest(String clientId, ClientConfiguration clientConfiguration) {
    String tokenKey = "tokens:" + clientId;
    String lastRefillKey = "lastRefill:" + clientId;

    long now = System.currentTimeMillis();
    Long lastRefillTime = storage.get(lastRefillKey);

    if (lastRefillTime == null) {
      storage.set(
          tokenKey, String.valueOf(clientConfiguration.getBurstCapacity()), Duration.ofMinutes(2));
      storage.set(lastRefillKey, String.valueOf(now), Duration.ofMinutes(2));
      return consumeToken(tokenKey);
    }

    long timePassed = now - lastRefillTime;
    long tokensToAdd = (timePassed * clientConfiguration.getRequestsPerMinute()) / (60 * 1000);

    if (tokensToAdd > 0) {
      Long currentTokens = storage.get(tokenKey);
      currentTokens = currentTokens != null ? currentTokens : 0;

      long newTokens =
          Math.min(clientConfiguration.getBurstCapacity(), currentTokens + tokensToAdd);
      storage.set(tokenKey, String.valueOf(newTokens), Duration.ofMinutes(2));
      storage.set(lastRefillKey, String.valueOf(now), Duration.ofMinutes(2));
    }

    return consumeToken(tokenKey);
  }

  private boolean consumeToken(String tokensKey) {
    Long tokens = storage.get(tokensKey);
    if (tokens != null && tokens > 0) {
      storage.set(tokensKey, String.valueOf(tokens - 1), Duration.ofMinutes(2));
      return true;
    }
    return false;
  }
}
