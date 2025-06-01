package com.vbalan.rate_limiter.exception;

import com.vbalan.rate_limiter.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
        .body(new ErrorResponse("Rate limit exceeded"));
  }

  @ExceptionHandler(StorageInitializationException.class)
  public ResponseEntity<ErrorResponse> handleStorageInitializationException(
      StorageInitializationException ex) {
    log.error("Storage initialization exception", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("Unexpected error"));
  }
}
