# Use lightweight JDK image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /workspace

# Copy Maven wrapper and pom.xml first (leverage Docker cache)
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY .mvn .mvn

# Make sure Maven wrapper is executable
RUN chmod +x mvnw

# Copy the rest of the source code
COPY src ./src

# Expose Spring Boot port
EXPOSE 9090

# Run Spring Boot via Maven wrapper
CMD ["./mvnw", "spring-boot:run"]
