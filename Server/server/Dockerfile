# ── Stage 1: base ─────────────────────────────────────────────
# eclipse-temurin:17-jdk-alpine is ~340 MB vs ~500 MB for jammy
FROM eclipse-temurin:17-jdk-alpine AS base
WORKDIR /workspace
COPY mvnw mvnw
COPY pom.xml pom.xml
COPY .mvn .mvn
RUN chmod +x mvnw

# ── Stage 2: development (compose uses this stage) ────────────
# Source is mounted via compose volume for hot-reload.
FROM base AS dev
COPY src ./src
EXPOSE 9090
CMD ["./mvnw", "spring-boot:run"]

# ── Stage 3: builder ───────────────────────────────────────────
# Pre-download deps then compile to leverage layer cache.
FROM base AS builder
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ── Stage 4: production ────────────────────────────────────────
# eclipse-temurin:17-jre-alpine is ~85 MB — no compiler/SDK overhead.
FROM eclipse-temurin:17-jre-alpine AS production
WORKDIR /app
COPY --from=builder /workspace/target/*.jar app.jar
EXPOSE 9090
CMD ["java", "-jar", "app.jar"]
