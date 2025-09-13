package com.vbalan.rate_limiter.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response model for the Rate Limiter API.
 * 
 * <p>This class provides a consistent structure for all error responses
 * throughout the application, following REST API best practices.</p>
 * 
 * @author Victor Balan
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * A brief error identifier.
     */
    private String error;
    
    /**
     * A human-readable message describing the error.
     */
    private String message;
    
    /**
     * The timestamp when the error occurred.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    /**
     * The request path where the error occurred.
     */
    private String path;
    
    /**
     * Additional error details (e.g., validation errors).
     */
    private Map<String, Object> details;
    
    /**
     * Convenience constructor for simple error responses.
     * 
     * @param error the error message
     */
    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }
}
