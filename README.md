# Rate Limiter Application

A Spring Boot application demonstrating two different rate limiting algorithms: Token Bucket and Sliding Window. The
application supports both in-memory and Redis-based storage and provides REST APIs with documentation via Swagger UI.
I've chosen to provide a docker file that takes on the building and running the application so that you don't need to
install java and maven on your local machine. For a ci/cd the approach will be to run the image build by the pipeline
runners with as few layers as possible for image size optimization and startup

## Features

- **Two Rate Limiting Algorithms**:
    - **Token Bucket**: Allows burst requests up to bucket capacity, then refills at steady rate
    - **Sliding Window**: Tracks individual request timestamps for precise rate limiting over sliding time window

- **Dual Storage Support**:
    - **Memory**: In-memory storage with automatic cleanup (default)
    - **Redis**: Distributed storage

- **Client-Based Configuration**: Different rate limits per client
- **API Documentation**: Interactive Swagger UI
- **Docker Support**: Containerized deployment with Docker Compose

## Prerequisites

### Option 1: Docker Only (Recommended)

- Docker Desktop or Docker Engine
- Docker Compose v2.0+

### Option 2: Local Development

- Java 21
- Maven 3.9+
- Redis Server (optional, for Redis profile)

## Quick Start

### Using Docker Compose

#### Memory Storage (Standalone)

```bash
# Build and run with in-memory storage
docker compose -f docker-compose-memory.yml up --build -d

# Check logs
docker compose -f docker-compose-memory.yml logs -f
```

#### Redis Storage

```bash
# Build and run with Redis storage
docker compose -f docker-compose-redis.yml up --build -d

# Check logs
docker compose -f docker-compose-redis.yml logs -f
```

#### Stopping the Application

```bash
# Stop memory version
docker compose -f docker-compose-memory.yml down

# Stop Redis version (with volume cleanup)
docker compose -f docker-compose-redis.yml down -v
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