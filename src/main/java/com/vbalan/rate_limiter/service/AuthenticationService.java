package com.vbalan.rate_limiter.service;

import com.vbalan.rate_limiter.config.ClientConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final ClientConfig clientConfig;

  public String extractClientId(String authorizationHeader) {
    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      return "";
    }
    return authorizationHeader.substring(7);
  }

  public boolean isValidClient(String clientId) {
    return clientId != null && clientConfig.getClients().containsKey(clientId);
  }
}
