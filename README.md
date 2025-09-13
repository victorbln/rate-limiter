# Professional Rate Limiter Service

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen?style=flat-square&logo=spring)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue?style=flat-square&logo=apache-maven)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square&logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](https://opensource.org/licenses/MIT)
[![Build Status](https://img.shields.io/badge/Build-Passing-success?style=flat-square)](https://github.com/victorbln/rate-limiter)

> A professional-grade, production-ready rate limiting service built with Spring Boot, demonstrating multiple algorithms and storage backends with comprehensive monitoring and documentation.

## 🚀 Features

### 🏗️ **Two Rate Limiting Algorithms**
- **Token Bucket**: Allows burst requests up to bucket capacity, then refills at steady rate
- **Sliding Window**: Tracks individual request timestamps for precise rate limiting over sliding time window

### 💾 **Dual Storage Support**
- **Memory**: In-memory storage with automatic cleanup (perfect for development)
- **Redis**: Distributed storage for production deployments

### 🔧 **Production-Ready Features**
- ✅ **Client-Based Configuration**: Different rate limits per client
- ✅ **Comprehensive API Documentation**: Interactive Swagger UI with detailed examples
- ✅ **Monitoring & Observability**: Actuator endpoints, Prometheus metrics
- ✅ **Professional Error Handling**: Structured error responses with detailed information
- ✅ **Docker Support**: Optimized containerized deployment with health checks
- ✅ **Multiple Profiles**: Development, production, and custom configurations
- ✅ **Code Quality**: Integrated static analysis and test coverage

## 📋 Table of Contents

- [Quick Start](#quick-start)
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Monitoring](#monitoring)
- [Development](#development)
- [Contributing](#contributing)
- [License](#license)

## Prerequisites

### Option 1: Docker Only (Recommended)

- Docker Desktop or Docker Engine
- Docker Compose v2.0+

### Option 2: Local Development

- Java 21
- Maven 3.9+
- Redis Server (optional, for Redis profile)

## ⚡ Quick Start

Get the Rate Limiter service running in under 2 minutes:

```bash
# Option 1: Using Docker (Recommended)
git clone https://github.com/victorbln/rate-limiter.git
cd rate-limiter
docker compose -f docker-compose-memory.yml up --build -d

# Option 2: Using Maven (requires Java 17+)
git clone https://github.com/victorbln/rate-limiter.git
cd rate-limiter
mvn spring-boot:run
```

🎉 **That's it!** Your rate limiter is now running at:
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus

### 🧪 Test the API

```bash
# Test Token Bucket algorithm (allows burst)
curl -H "Authorization: Bearer client-1" http://localhost:8080/foo

# Test Sliding Window algorithm (strict limiting)
curl -H "Authorization: Bearer client-2" http://localhost:8080/bar
```

### Using IntelliJ IDEA

#### 1. Import Project

1. Open IntelliJ IDEA
2. File -> Open -> Select the project root directory
3. Wait for Maven to import dependencies

#### 2. Configure Run Configurations

**For Memory Storage:**

1. Run -> Edit Configurations
2. Add New Configuration -> Application
3. Configure:
    - Name: `Rate Limiter - Memory`
    - Main class: `com.vbalan.rate_limiter.RateLimiterApplication`
    - VM options: `-Dspring.profiles.active=memory`

**For Redis Storage:**

1. Start Redis locally:
   ```bash
   docker run -d --name redis -p 6379:6379 redis:latest
   ```

2. Add New Configuration -> Application
3. Configure:
    - Name: `Rate Limiter - Redis`
    - Main class: `com.vbalan.rate_limiter.RateLimiterApplication`
    - VM options: `-Dspring.profiles.active=redis`

#### 3. Running Tests in IntelliJ

- Right-click on `src/test/java` -> Run 'All Tests'
- Or right-click on specific test class -> Run 'TestClassName'

## Using the Application

### Accessing Swagger UI

Once the application is running, open your browser and navigate to:

- **Swagger UI**: http://localhost:8080/swagger-ui.html

### Using Swagger UI

1. **Open Swagger UI** in your browser at http://localhost:8080/swagger-ui.html
2. **View API Documentation**: You'll see two main endpoints:
    - `/foo` - Token Bucket rate limiting
    - `/bar` - Sliding Window rate limiting

3. **Authenticate**:
    - Click the **Authorize** button at the top right
    - Enter one of the valid client IDs: `client-1` or `client-2`
    - Click **Authorize** and then **Close**

4. **Test Endpoints**:
    - Click on any endpoint (e.g., `GET /foo`)
    - Click **Try it out**
    - Click **Execute**
    - View the response

5. **Test Rate Limiting**:
    - Execute the same endpoint multiple times quickly
    - Observe how responses change when rate limits are exceeded
    - Try different client IDs to see different rate limit configurations

### Client Configurations

- **client-1**: 5 requests/minute, burst capacity: 3
- **client-2**: 15 requests/minute, burst capacity: 8

### Manual Testing with curl

#### Test Token Bucket Algorithm (/foo)

**Linux/macOS/Git Bash:**

```bash
# Test with client-1 (allows 3 burst requests)
for i in {1..6}; do
  echo "Request $i:"
  curl -H "Authorization: Bearer client-1" \
       -w "\nStatus: %{http_code}\n" \
       http://localhost:8080/foo
  echo "---"
done
```

**Windows PowerShell:**

```powershell
for ($i = 1; $i -le 6; $i++) {
    Write-Host "Request $i"
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/foo" -Headers @{"Authorization" = "Bearer client-1"}
        Write-Host "Status: $($response.StatusCode)"
        Write-Host $response.Content
    } catch {
        Write-Host "Status: $($_.Exception.Response.StatusCode)"
    }
    Write-Host "---"
}
```

#### Test Sliding Window Algorithm (/bar)

**Linux/macOS/Git Bash:**

```bash
# Test with client-2 (allows 15 requests per minute)
for i in {1..18}; do
  echo "Request $i:"
  curl -H "Authorization: Bearer client-2" \
       -w "\nStatus: %{http_code}\n" \
       http://localhost:8080/bar
  echo "---"
  sleep 2
done
```

**Windows PowerShell:**

```powershell
for ($i = 1; $i -le 18; $i++) {
    Write-Host "Request $i"
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/bar" -Headers @{"Authorization" = "Bearer client-2"}
        Write-Host "Status: $($response.StatusCode)"
        Write-Host $response.Content
    } catch {
        Write-Host "Status: $($_.Exception.Response.StatusCode)"
    }
    Write-Host "---"
    Start-Sleep -Seconds 2
}
```

### Expected Responses

**Success (200 OK):**

```json
{
  "success": true
}
```

**Rate Limited (429 Too Many Requests):**

```json
{
  "error": "Rate limit exceeded"
}
```

**Unauthorized (401):**

```plain text
No Authorization header provided
```

## Configuration

### Application Configuration

The application is configured via `application.yml`:

```yaml
rate-limit:
  clients:
    client-1:
      requests-per-minute: 5
      burst-capacity: 3
    client-2:
      requests-per-minute: 15
      burst-capacity: 8
  storage:
    type: memory  # or 'redis'
```

### Adding New Clients

To add a new client, edit `application.yml`:

```yaml
rate-limit:
  clients:
    new-client:
      requests-per-minute: 10
      burst-capacity: 5
```

Then restart the application.

## Rate Limiting Algorithms

### Token Bucket (/foo endpoint)

- Each client has a "bucket" with maximum capacity (burst-capacity)
- Tokens are added at a steady rate (requests-per-minute)
- Each request consumes one token
- Allows burst traffic up to bucket capacity
- Good for applications that need to handle traffic spikes

### Sliding Window (/bar endpoint)

- Maintains a sliding 60-second time window
- Tracks exact timestamps of each request
- Removes old requests as the window slides
- Provides precise rate limiting
- Better for strict rate enforcement

## Monitoring

### Application Logs

```bash
# Docker Compos
docker compose -f docker-compose-redis.yml logs -f rate-limiter-app

# Individual container
docker logs <container-name> -f
```

### Redis Monitoring (if using Redis)

Install redis insight and connect to localhost:6379. You will be able to see the stored data when making requests to the
endpoints.

## Troubleshooting

### Port Already in Use

```bash
# Check what's using port 8080
lsof -i :8080

# Stop existing containers
docker compose down
```

### Redis Connection Issues

```bash
# Check Redis is running
docker ps | grep redis

# Test Redis connectivity
docker exec -it <redis-container> redis-cli ping
```

### Application Won't Start

```bash
# Check application logs
docker compose logs rate-limiter-app

# Common solutions:
# 1. Ensure Java 21 is available
# 2. Check port 8080 is available
# 3. Verify Redis is running (for Redis profile)
```

## API Reference

### Authentication

All endpoints require Bearer token authentication:

```
Authorization: Bearer <client-id>
```

Valid client IDs: `client-1`, `client-2`

## Development

### Running Tests

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=TokenBucketRateLimiterTest
```

### Building

```bash
mvn clean compile

mvn clean package -DskipTests
```

## Author

Victor Balan - victorbalan9@gmail.com

## Quick Commands Reference

```bash
# Docker Compose
docker compose -f docker-compose-memory.yml up --build -d
docker compose -f docker-compose-redis.yml up --build -d
docker compose down

# Maven
mvn clean compile
mvn test
mvn spring-boot:run -Dspring-boot.run.profiles=memory

# Testing
curl -H "Authorization: Bearer client-1" http://localhost:8080/foo
curl -H "Authorization: Bearer client-2" http://localhost:8080/bar

# Monitoring
docker compose logs -f rate-limiter-app
```