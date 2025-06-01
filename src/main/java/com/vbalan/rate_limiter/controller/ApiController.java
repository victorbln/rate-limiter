package com.vbalan.rate_limiter.controller;

import com.vbalan.rate_limiter.exception.RateLimitExceededException;
import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.model.CustomApiResponse;
import com.vbalan.rate_limiter.service.AuthenticationService;
import com.vbalan.rate_limiter.service.RateLimitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(
    name = "Rate Limited Endpoints",
    description = "API endpoints demonstrating different rate limiting algorithms")
public class ApiController {

  private final AuthenticationService authenticationService;
  private final RateLimitService rateLimitService;

  @GetMapping("/foo")
  @Operation(
      summary = "Token Bucket Rate Limited Endpoint",
      description =
          "This endpoint uses Token Bucket algorithm for rate limiting. "
              + "Allows burst requests up to the bucket capacity, then refills at a steady rate.")
  public ResponseEntity<CustomApiResponse> foo(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorization) {
    String clientId = authenticationService.extractClientId(authorization);
    if (!authenticationService.isValidClient(clientId)) {
      return ResponseEntity.status(401).build();
    }

    ClientConfiguration config = authenticationService.getClientConfiguration(clientId);

    if (!rateLimitService.allowRequestForFoo(clientId, config)) {
      throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
    }

    return ResponseEntity.ok(new CustomApiResponse(true));
  }

  @GetMapping("/bar")
  @Operation(
      summary = "Sliding Window Rate Limited Endpoint",
      description =
          "This endpoint uses Sliding Window algorithm for rate limiting. "
              + "Tracks individual request timestamps for precise rate limiting over a sliding time window.")
  public ResponseEntity<CustomApiResponse> bar(
      @Parameter(hidden = true) @RequestHeader("Authorization") String authorization) {
    String clientId = authenticationService.extractClientId(authorization);

    if (!authenticationService.isValidClient(clientId)) {
      return ResponseEntity.status(401).build();
    }

    ClientConfiguration config = authenticationService.getClientConfiguration(clientId);

    if (!rateLimitService.allowRequestForBar(clientId, config)) {
      throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
    }

    return ResponseEntity.ok(new CustomApiResponse(true));
  }
}
