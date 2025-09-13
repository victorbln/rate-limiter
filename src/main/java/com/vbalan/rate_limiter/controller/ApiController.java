package com.vbalan.rate_limiter.controller;

import com.vbalan.rate_limiter.exception.RateLimitExceededException;
import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.model.CustomApiResponse;
import com.vbalan.rate_limiter.model.ErrorResponse;
import com.vbalan.rate_limiter.service.AuthenticationService;
import com.vbalan.rate_limiter.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;

/**
 * REST controller providing rate-limited API endpoints.
 * 
 * <p>This controller demonstrates two different rate limiting algorithms:</p>
 * <ul>
 *   <li><strong>Token Bucket</strong> (/foo): Allows burst traffic up to bucket capacity</li>
 *   <li><strong>Sliding Window</strong> (/bar): Provides precise rate limiting over time window</li>
 * </ul>
 * 
 * <p>All endpoints require Bearer token authentication with valid client IDs.</p>
 * 
 * @author Victor Balan
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Rate Limited Endpoints",
    description = "API endpoints demonstrating different rate limiting algorithms with client-specific configurations")
@SecurityRequirement(name = "bearerAuth")
public class ApiController {

  private final AuthenticationService authenticationService;
  private final RateLimitService rateLimitService;

  /**
   * Token Bucket rate limited endpoint.
   * 
   * @param authorization the authorization header with Bearer token
   * @return success response if request is allowed
   * @throws RateLimitExceededException if rate limit is exceeded
   */
  @GetMapping(value = "/foo", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Token Bucket Rate Limited Endpoint",
      description = """
          Demonstrates the Token Bucket rate limiting algorithm.
          
          **Algorithm Details:**
          - Each client has a virtual "bucket" with maximum capacity (burst-capacity)
          - Tokens are added to the bucket at a steady rate (requests-per-minute / 60 seconds)
          - Each request consumes one token from the bucket
          - Requests are allowed if tokens are available, rejected otherwise
          - Allows burst traffic up to bucket capacity, then enforces steady rate
          
          **Use Cases:**
          - Applications that need to handle traffic spikes
          - APIs that can tolerate burst requests
          - Systems where occasional high throughput is acceptable
          
          **Client Configurations:**
          - client-1: 5 requests/minute, burst capacity: 3 tokens
          - client-2: 15 requests/minute, burst capacity: 8 tokens
          """)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Request allowed - rate limit not exceeded",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomApiResponse.class),
              examples = @ExampleObject(
                  name = "Success",
                  value = "{\"success\": true}"))),
      @ApiResponse(
          responseCode = "401",
          description = "Unauthorized - invalid or missing authorization header",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "Unauthorized",
                  value = "{\"error\": \"Unauthorized\", \"message\": \"Authorization header is required\"}"))),
      @ApiResponse(
          responseCode = "429",
          description = "Too Many Requests - rate limit exceeded",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "Rate Limited",
                  value = "{\"error\": \"Rate limit exceeded\", \"message\": \"Rate limit exceeded for client: client-1\"}")))
  })
  public ResponseEntity<CustomApiResponse> foo(
      @Parameter(
          description = "Bearer token with client ID (e.g., 'Bearer client-1')",
          required = true,
          example = "Bearer client-1")
      @RequestHeader("Authorization") 
      @NotBlank(message = "Authorization header cannot be blank") 
      String authorization) {
    
    log.debug("Received token bucket request with authorization: {}", 
        authorization.replaceAll("Bearer\\s+", "Bearer ***"));
    
    String clientId = authenticationService.extractClientId(authorization);
    if (!authenticationService.isValidClient(clientId)) {
      log.warn("Invalid client ID: {}", clientId);
      return ResponseEntity.status(401).build();
    }

    ClientConfiguration config = authenticationService.getClientConfiguration(clientId);
    log.debug("Processing token bucket request for client: {} with config: {}", clientId, config);

    if (!rateLimitService.allowRequestForFoo(clientId, config)) {
      throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
    }

    log.debug("Token bucket request allowed for client: {}", clientId);
    return ResponseEntity.ok(new CustomApiResponse(true));
  }

  /**
   * Sliding Window rate limited endpoint.
   * 
   * @param authorization the authorization header with Bearer token
   * @return success response if request is allowed
   * @throws RateLimitExceededException if rate limit is exceeded
   */
  @GetMapping(value = "/bar", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Sliding Window Rate Limited Endpoint", 
      description = """
          Demonstrates the Sliding Window rate limiting algorithm.
          
          **Algorithm Details:**
          - Maintains a sliding 60-second time window
          - Tracks exact timestamps of each request within the window
          - Automatically removes old requests as the window slides forward
          - Provides precise rate limiting without allowing burst beyond the configured rate
          - Memory usage grows with request frequency but provides exact enforcement
          
          **Use Cases:**
          - Applications requiring strict rate enforcement
          - APIs where consistent request distribution is important
          - Systems that need precise rate limiting without burst allowance
          
          **Client Configurations:**
          - client-1: 5 requests/minute (no burst allowed)
          - client-2: 15 requests/minute (no burst allowed)
          """)
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Request allowed - rate limit not exceeded",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = CustomApiResponse.class),
              examples = @ExampleObject(
                  name = "Success",
                  value = "{\"success\": true}"))),
      @ApiResponse(
          responseCode = "401", 
          description = "Unauthorized - invalid or missing authorization header",
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "Unauthorized",
                  value = "{\"error\": \"Unauthorized\", \"message\": \"Authorization header is required\"}"))),
      @ApiResponse(
          responseCode = "429",
          description = "Too Many Requests - rate limit exceeded", 
          content = @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(
                  name = "Rate Limited",
                  value = "{\"error\": \"Rate limit exceeded\", \"message\": \"Rate limit exceeded for client: client-2\"}")))
  })
  public ResponseEntity<CustomApiResponse> bar(
      @Parameter(
          description = "Bearer token with client ID (e.g., 'Bearer client-2')",
          required = true,
          example = "Bearer client-2")
      @RequestHeader("Authorization") 
      @NotBlank(message = "Authorization header cannot be blank")
      String authorization) {
    
    log.debug("Received sliding window request with authorization: {}", 
        authorization.replaceAll("Bearer\\s+", "Bearer ***"));

    String clientId = authenticationService.extractClientId(authorization);
    if (!authenticationService.isValidClient(clientId)) {
      log.warn("Invalid client ID: {}", clientId);
      return ResponseEntity.status(401).build();
    }

    ClientConfiguration config = authenticationService.getClientConfiguration(clientId);
    log.debug("Processing sliding window request for client: {} with config: {}", clientId, config);

    if (!rateLimitService.allowRequestForBar(clientId, config)) {
      throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
    }

    log.debug("Sliding window request allowed for client: {}", clientId);
    return ResponseEntity.ok(new CustomApiResponse(true));
  }
}
