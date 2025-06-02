# This docker file isn't optimized as it is handling the build by itself in the first part which should be the responsability
#of a ci/cd pipeline. I chose to make it this way so that you don't need java or maven in your machine to run the application,
# just docker. It is used by both docker compose files and documentation about its usage can be found in the README.md

#build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

# runtime
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

WORKDIR /app

COPY --from=build /app/target/rate-limiter-*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseContainerSupport"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]