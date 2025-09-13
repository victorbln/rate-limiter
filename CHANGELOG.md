# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Professional-grade API documentation with comprehensive OpenAPI/Swagger specifications
- Enhanced error handling with structured error responses
- Monitoring and observability features with Spring Boot Actuator
- Prometheus metrics support for production monitoring
- Comprehensive logging with structured output
- Professional build configuration with Maven profiles
- Code quality tools integration (Checkstyle, SpotBugs, JaCoCo)
- Docker health checks and optimization
- Professional-grade configuration management with multiple profiles

### Changed
- Upgraded to Java 17 for broader compatibility (from Java 21)
- Enhanced error responses with detailed information and timestamps
- Improved API controller with comprehensive documentation and validation
- Enhanced OpenAPI configuration with detailed endpoint descriptions
- Updated application configuration with production-ready settings
- Improved in-memory storage implementation with proper lifecycle management

### Fixed
- Java 17 compatibility issues in InMemoryStorage
- Try-with-resources usage in cleanup scheduler
- Exception handling type mismatches
- Test expectations to match new error response format

### Security
- Added input validation with Bean Validation
- Enhanced authentication error responses
- Improved security headers configuration

## [1.0.0] - 2025-01-13

### Added
- Initial implementation of Rate Limiter service
- Token Bucket rate limiting algorithm
- Sliding Window rate limiting algorithm
- In-memory storage backend
- Redis storage backend
- REST API with two endpoints (/foo and /bar)
- Client-based configuration support
- Docker and Docker Compose support
- Basic API documentation with Swagger UI
- Comprehensive test suite
- Spring Boot 3 integration

### Features
- **Rate Limiting Algorithms:**
  - Token Bucket: Allows burst requests up to bucket capacity
  - Sliding Window: Precise rate limiting over time windows

- **Storage Backends:**
  - In-memory: For development and single-instance deployments
  - Redis: For production and distributed deployments

- **Configuration:**
  - Client-specific rate limits
  - Configurable burst capacity
  - Multiple storage profiles

- **API:**
  - `/foo` - Token Bucket rate limited endpoint
  - `/bar` - Sliding Window rate limited endpoint
  - Bearer token authentication
  - JSON responses

- **Infrastructure:**
  - Docker multi-stage builds
  - Docker Compose configurations for different scenarios
  - Maven build system
  - Spring Boot framework