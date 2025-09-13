package com.vbalan.rate_limiter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

/**
 * Main application class for the Rate Limiter service.
 * 
 * <p>This Spring Boot application provides a professional-grade rate limiting service
 * with support for multiple algorithms (Token Bucket and Sliding Window) and storage
 * backends (In-Memory and Redis).</p>
 * 
 * <p>Key features:</p>
 * <ul>
 *   <li>RESTful API with comprehensive documentation</li>
 *   <li>Multiple rate limiting algorithms</li>
 *   <li>Flexible storage backends</li>
 *   <li>Production-ready monitoring and health checks</li>
 *   <li>Docker and Kubernetes support</li>
 * </ul>
 * 
 * @author Victor Balan
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@SpringBootApplication
public class RateLimiterApplication {

  /**
   * Main method to start the Rate Limiter application.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    try {
      SpringApplication app = new SpringApplication(RateLimiterApplication.class);
      app.run(args);
    } catch (Exception e) {
      log.error("Failed to start Rate Limiter application", e);
      System.exit(1);
    }
  }

  /**
   * Log application startup information when the application is ready.
   * 
   * @param event the application ready event
   */
  @EventListener(ApplicationReadyEvent.class)
  public void onApplicationReady(ApplicationReadyEvent event) {
    Environment env = event.getApplicationContext().getEnvironment();
    String serverPort = env.getProperty("server.port", "8080");
    String activeProfiles = String.join(", ", env.getActiveProfiles());
    String contextPath = env.getProperty("server.servlet.context-path", "");
    
    log.info("═══════════════════════════════════════════════════════════════");
    log.info("  Rate Limiter Application Started Successfully!");
    log.info("  Port(s): {} (http)", serverPort);
    log.info("  Profile(s): {}", activeProfiles.isEmpty() ? "default" : activeProfiles);
    log.info("  Application URL: http://localhost:{}{}", serverPort, contextPath);
    log.info("  Swagger UI: http://localhost:{}{}/swagger-ui.html", serverPort, contextPath);
    log.info("  Health Check: http://localhost:{}{}/actuator/health", serverPort, contextPath);
    log.info("  Metrics: http://localhost:{}{}/actuator/metrics", serverPort, contextPath);
    log.info("═══════════════════════════════════════════════════════════════");
  }
}
