package com.vbalan.rate_limiter.controller;

import com.vbalan.rate_limiter.exception.RateLimitExceededException;
import com.vbalan.rate_limiter.model.ApiResponse;
import com.vbalan.rate_limiter.model.ClientConfiguration;
import com.vbalan.rate_limiter.service.AuthenticationService;
import com.vbalan.rate_limiter.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ApiController {

  private final AuthenticationService authenticationService;
  private final RateLimitService rateLimitService;

  @GetMapping("/foo")
  public ResponseEntity<ApiResponse> foo(@RequestHeader("Authorization") String authorization) {
    String clientId = authenticationService.extractClientId(authorization);

    if (!authenticationService.isValidClient(clientId)) {
      return ResponseEntity.status(401).build();
    }

    ClientConfiguration config = authenticationService.getClientConfiguration(clientId);

    if (!rateLimitService.allowRequestForFoo(clientId, config)) {
      throw new RateLimitExceededException("Rate limit exceeded for client: " + clientId);
    }

    return ResponseEntity.ok(new ApiResponse(true));
  }

  @GetMapping("/bar")
  public ResponseEntity<ApiResponse> bar(@RequestHeader("Authorization") String authorization) {
    String clientId = authenticationService.extractClientId(authorization);

    if (!authenticationService.isValidClient(clientId)) {
      return ResponseEntity.status(401).build();
    }

    return ResponseEntity.ok(new ApiResponse(true));
  }
}
