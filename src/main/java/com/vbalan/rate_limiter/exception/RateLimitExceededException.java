package com.vbalan.rate_limiter.exception;

public class RateLimitExceededException extends RuntimeException {
  public RateLimitExceededException(String message) {
    super(message);
  }
}