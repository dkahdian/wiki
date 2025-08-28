# Multi-stage build that replicates Spring Boot buildpack behavior
FROM ghcr.io/graalvm/native-image-community:21 AS builder

WORKDIR /workspace

# Copy Maven wrapper and configuration
COPY .mvn .mvn  
COPY mvnw mvnw.cmd pom.xml ./
RUN chmod +x mvnw

# Download dependencies first (for caching)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build native executable (same as what buildpack does internally)
RUN ./mvnw native:compile -Pnative -DskipTests
RUN chmod +x target/wiki

# Runtime stage - match builder OS/glibc to avoid ABI mismatches
FROM ghcr.io/graalvm/native-image-community:21

# Copy native executable
COPY --from=builder /workspace/target/wiki /app/wiki

# Executable bit already set in builder; matching base ensures glibc compatibility

EXPOSE 8080

ENTRYPOINT ["/app/wiki"]
