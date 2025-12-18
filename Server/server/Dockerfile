# Development image for Spring Boot app
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . /workspace

# run a Maven build to populate /root/.m2 with dependencies and build artifacts
RUN mvn -q -DskipTests=true package -DskipITs=true

# Use a lightweight runtime image for running the app in dev mode
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /workspace
# copy maven cache from builder if available
COPY --from=build /root/.m2 /root/.m2
COPY --from=build /workspace /workspace
WORKDIR /workspace

ENV MAVEN_OPTS "-Xmx512m"
# Run the app in dev mode using mvn spring-boot:run so changes mounted from host reflect
CMD ["/bin/sh", "-c", "mvn -q -DskipTests=true spring-boot:run"]
EXPOSE 9090
