package com.vbalan.rate_limiter.config;

import com.vbalan.rate_limiter.model.ClientConfiguration;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limit")
@Data
public class ClientConfig {
  private Map<String, ClientConfiguration> clients = new HashMap<>();
}
