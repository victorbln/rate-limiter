# Security Policy

## Supported Versions

We actively support the following versions of the Rate Limiter service:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security vulnerability, please follow these steps:

### 1. Do Not Create Public Issues
Please do not create public GitHub issues for security vulnerabilities.

### 2. Contact Us Privately
Send an email to [victorbalan9@gmail.com](mailto:victorbalan9@gmail.com) with:
- A clear description of the vulnerability
- Steps to reproduce the issue
- Potential impact assessment
- Any suggested fixes (optional)

### 3. Response Timeline
- **Initial Response**: Within 48 hours
- **Confirmation**: Within 1 week
- **Fix Timeline**: Depends on severity
  - Critical: Within 1 week
  - High: Within 2 weeks
  - Medium: Within 1 month
  - Low: Next regular release

### 4. Disclosure Policy
- We follow responsible disclosure practices
- We will acknowledge your contribution in the fix announcement
- We request that you do not publicly disclose the vulnerability until we have released a fix

## Security Best Practices

When using the Rate Limiter service:

### Authentication
- Always use secure client IDs in production
- Rotate client credentials regularly
- Use environment variables for sensitive configuration
- Never commit credentials to version control

### Network Security
- Deploy behind a reverse proxy (nginx, Apache)
- Use HTTPS in production
- Implement proper firewall rules
- Consider using VPN for internal services

### Monitoring
- Monitor rate limiting metrics
- Set up alerts for unusual patterns
- Log authentication failures
- Review logs regularly

### Configuration
- Use the production profile in production environments
- Secure Redis instances with authentication
- Use secure Redis connection (TLS)
- Implement proper backup strategies

## Known Security Considerations

### Input Validation
- Client IDs are validated for format and existence
- All HTTP headers are properly sanitized
- JSON responses are properly escaped

### Rate Limiting
- The service itself should be rate limited at the infrastructure level
- Consider implementing circuit breakers for external dependencies

### Dependencies
- Dependencies are regularly updated
- Security scanning is performed on dependencies
- Known vulnerabilities are addressed promptly

## Security Hardening

### Docker Deployment
```dockerfile
# Use non-root user
USER appuser

# Set resource limits
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health checks
HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/actuator/health
```

### Production Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: never  # Hide sensitive details in production
```

## Incident Response

In case of a security incident:
1. Immediately contact the security team
2. Document all relevant information
3. Isolate affected systems if necessary
4. Follow your organization's incident response procedures

## Compliance

This service can be configured to meet various compliance requirements:
- GDPR: No personal data is stored by default
- SOC 2: Comprehensive logging and monitoring capabilities
- PCI DSS: Secure configuration options available

## Updates

This security policy is reviewed and updated regularly. Check back for the latest version.