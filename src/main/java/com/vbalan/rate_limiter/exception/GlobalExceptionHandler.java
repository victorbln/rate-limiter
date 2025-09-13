package com.vbalan.rate_limiter.exception;

import com.vbalan.rate_limiter.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Rate Limiter application.
 * 
 * <p>This class provides centralized exception handling across the whole application.
 * It ensures consistent error responses and proper logging of exceptions.</p>
 * 
 * @author Victor Balan
 * @since 1.0.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  /**
   * Handles rate limit exceeded exceptions.
   * 
   * @param ex the rate limit exceeded exception
   * @param request the web request
   * @return a 429 Too Many Requests response
   */
  @ExceptionHandler(RateLimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
      RateLimitExceededException ex, WebRequest request) {
    log.warn("Rate limit exceeded: {} for request: {}", ex.getMessage(), request.getDescription(false));
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Rate limit exceeded")
        .message(ex.getMessage())
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .build();
        
    return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse);
  }

  /**
   * Handles storage initialization exceptions.
   * 
   * @param ex the storage initialization exception
   * @param request the web request
   * @return a 500 Internal Server Error response
   */
  @ExceptionHandler(StorageInitializationException.class)
  public ResponseEntity<ErrorResponse> handleStorageInitializationException(
      StorageInitializationException ex, WebRequest request) {
    log.error("Storage initialization exception for request: {}", request.getDescription(false), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Service Unavailable")
        .message("The service is temporarily unavailable")
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .build();
        
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Handles missing authorization header exceptions.
   * 
   * @param ex the missing request header exception
   * @param request the web request
   * @return a 401 Unauthorized response
   */
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(
      MissingRequestHeaderException ex, WebRequest request) {
    log.warn("Missing authorization header for request: {}", request.getDescription(false));
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Unauthorized")
        .message("Authorization header is required")
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .build();
        
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
  }

  /**
   * Handles validation errors.
   * 
   * @param ex the method argument not valid exception
   * @param request the web request
   * @return a 400 Bad Request response with validation details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    
    Map<String, Object> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Validation failed for request: {} with errors: {}", request.getDescription(false), errors);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Validation Failed")
        .message("Invalid input parameters")
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .details(errors)
        .build();
        
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles method argument type mismatch exceptions.
   * 
   * @param ex the method argument type mismatch exception
   * @param request the web request
   * @return a 400 Bad Request response
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex, WebRequest request) {
    
    log.warn("Type mismatch for parameter '{}' in request: {}", ex.getName(), request.getDescription(false));
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Bad Request")
        .message(String.format("Invalid value for parameter '%s': %s", ex.getName(), ex.getValue()))
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .build();
        
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles all other unexpected exceptions.
   * 
   * @param ex the unexpected exception
   * @param request the web request
   * @return a 500 Internal Server Error response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
    log.error("Unexpected error for request: {}", request.getDescription(false), ex);
    
    ErrorResponse errorResponse = ErrorResponse.builder()
        .error("Internal Server Error")
        .message("An unexpected error occurred")
        .timestamp(LocalDateTime.now())
        .path(extractPath(request))
        .build();
        
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }

  /**
   * Extracts the request path from the WebRequest.
   * 
   * @param request the web request
   * @return the request path
   */
  private String extractPath(WebRequest request) {
    String description = request.getDescription(false);
    // Extract URI from description like "uri=/foo"
    if (description.startsWith("uri=")) {
      return description.substring(4);
    }
    return description;
  }
}
