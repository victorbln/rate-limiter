package com.vbalan.rate_limiter.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) configuration for the Rate Limiter API.
 * 
 * <p>This configuration provides comprehensive API documentation with:</p>
 * <ul>
 *   <li>Detailed endpoint descriptions and examples</li>
 *   <li>Authentication requirements and schemes</li>
 *   <li>Response models and error handling documentation</li>
 *   <li>Contact information and external links</li>
 * </ul>
 * 
 * @author Victor Balan
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

  @Value("${server.port:8080}")
  private String serverPort;
  
  @Value("${server.servlet.context-path:}")
  private String contextPath;

  /**
   * Configures the OpenAPI specification for the Rate Limiter API.
   * 
   * @return the configured OpenAPI instance
   */
  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .servers(List.of(
            new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Local development server"),
            new Server()
                .url("https://api.ratelimiter.example.com")
                .description("Production server (example)")
        ))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("Client-ID")
                        .description("Bearer authentication using client ID (e.g., 'Bearer client-1')")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .info(
            new Info()
                .title("Professional Rate Limiter API")
                .description("""
                    A professional-grade rate limiting service demonstrating multiple algorithms and storage backends.
                    
                    ## Features
                    
                    - **Two Rate Limiting Algorithms:**
                      - **Token Bucket**: Allows burst requests up to bucket capacity
                      - **Sliding Window**: Provides precise rate limiting over time windows
                    
                    - **Flexible Storage:**
                      - In-memory storage for development and testing
                      - Redis storage for production and distributed deployments
                    
                    - **Client-Based Configuration:**
                      - Different rate limits per client
                      - Configurable burst capacity
                      - Support for custom client configurations
                    
                    ## Authentication
                    
                    All endpoints require Bearer token authentication using valid client IDs:
                    - `client-1`: 5 requests/minute, burst capacity: 3
                    - `client-2`: 15 requests/minute, burst capacity: 8
                    
                    Example: `Authorization: Bearer client-1`
                    
                    ## Rate Limiting Algorithms
                    
                    ### Token Bucket (`/foo`)
                    Best for applications that can handle burst traffic. Allows requests up to the bucket 
                    capacity, then refills at a steady rate.
                    
                    ### Sliding Window (`/bar`)
                    Best for strict rate enforcement. Tracks exact request timestamps and provides 
                    precise limiting without burst allowance.
                    
                    ## Error Responses
                    
                    The API returns consistent error responses with detailed information:
                    - `401 Unauthorized`: Invalid or missing authentication
                    - `429 Too Many Requests`: Rate limit exceeded
                    - `500 Internal Server Error`: Server-side errors
                    """)
                .version("1.0.0")
                .contact(
                    new Contact()
                        .name("Victor Balan")
                        .email("victorbalan9@gmail.com")
                        .url("https://github.com/victorbln"))
                .license(
                    new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")))
        .externalDocs(
            new ExternalDocumentation()
                .description("Rate Limiter Documentation on GitHub")
                .url("https://github.com/victorbln/rate-limiter"));
  }
}
