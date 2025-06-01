package com.vbalan.rate_limiter.service;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RateLimitService {
  private final TokenBucketRateLimiter tokenBucketRateLimiter;

  public boolean allowRequestForFoo(String clientId, ClientConfiguration clientConfiguration) {
    return tokenBucketRateLimiter.allowRequest(clientId, clientConfiguration);
  }
}
