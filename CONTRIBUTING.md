# Contributing to Rate Limiter

Thank you for your interest in contributing to the Rate Limiter project! This document provides guidelines for contributing to the project.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.9+
- Docker (optional, for containerized development)
- Git

### Development Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/victorbln/rate-limiter.git
   cd rate-limiter
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

### Using Docker

Alternatively, you can use Docker for development:

```bash
# Build and run with in-memory storage
docker compose -f docker-compose-memory.yml up --build

# Or with Redis storage
docker compose -f docker-compose-redis.yml up --build
```

## Development Guidelines

### Code Style

- Follow Java coding conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public APIs
- Maintain consistent formatting

### Testing

- Write unit tests for all new functionality
- Maintain or improve test coverage
- Use descriptive test method names
- Follow the Arrange-Act-Assert pattern

### Commit Messages

Use conventional commit format:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

Types:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation only changes
- `style`: Changes that do not affect the meaning of the code
- `refactor`: A code change that neither fixes a bug nor adds a feature
- `test`: Adding missing tests or correcting existing tests
- `chore`: Changes to the build process or auxiliary tools

Examples:
- `feat(api): add new rate limiting algorithm`
- `fix(storage): resolve memory leak in cleanup task`
- `docs(readme): update installation instructions`

### Pull Request Process

1. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes and commit them:**
   ```bash
   git add .
   git commit -m "feat: add your feature description"
   ```

3. **Push to your branch:**
   ```bash
   git push origin feature/your-feature-name
   ```

4. **Create a Pull Request:**
   - Use a clear and descriptive title
   - Provide a detailed description of your changes
   - Link any relevant issues
   - Ensure all tests pass
   - Request review from maintainers

### Code Review Guidelines

- Be respectful and constructive in reviews
- Focus on the code, not the person
- Explain your reasoning for requested changes
- Suggest specific improvements when possible

## Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/vbalan/rate_limiter/
│   │       ├── config/          # Configuration classes
│   │       ├── controller/      # REST controllers
│   │       ├── exception/       # Exception handling
│   │       ├── model/          # Data models
│   │       ├── service/        # Business logic
│   │       └── storage/        # Storage implementations
│   └── resources/
│       └── application.yml     # Application configuration
└── test/
    └── java/                   # Test classes
```

## Adding New Features

### Rate Limiting Algorithms

To add a new rate limiting algorithm:

1. Create a new service class implementing rate limiting logic
2. Update the `RateLimitService` to use the new algorithm
3. Add corresponding tests
4. Update documentation

### Storage Backends

To add a new storage backend:

1. Implement the `RateLimitStorage` interface
2. Add configuration for the new storage type
3. Update the storage factory/configuration
4. Add integration tests
5. Update documentation

## Running Quality Checks

### Code Quality

```bash
# Run with code quality checks
mvn clean compile -P code-quality
```

### Test Coverage

```bash
# Generate test coverage report
mvn clean test jacoco:report
```

Coverage reports are generated in `target/site/jacoco/index.html`.

### Static Analysis

The project includes SpotBugs and Checkstyle for static analysis:

```bash
mvn clean compile -P code-quality
```

## Documentation

- Update README.md for user-facing changes
- Add JavaDoc for new public APIs
- Update API documentation in OpenAPI annotations
- Include code examples where appropriate

## Performance Considerations

- Consider memory usage for new storage implementations
- Test performance under load
- Document any performance characteristics
- Profile before and after changes when relevant

## Security Guidelines

- Never commit sensitive information (passwords, API keys, etc.)
- Follow secure coding practices
- Validate all inputs
- Use appropriate authentication and authorization
- Report security vulnerabilities privately

## Getting Help

- Open an issue for bug reports or feature requests
- Use discussions for questions and general help
- Check existing issues before creating new ones
- Provide minimal reproducible examples

## License

By contributing to this project, you agree that your contributions will be licensed under the MIT License.