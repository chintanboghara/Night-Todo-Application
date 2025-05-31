# Stage 1: Build the application using Maven
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Copy pom.xml and download dependencies first (improves build caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code
COPY src ./src

# Package the application (skip tests for faster builds)
RUN mvn clean package -DskipTests


# Stage 2: Create the final runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S -G appgroup appuser

# Copy the packaged jar from the build stage
COPY --from=build /app/target/todo-app-1.0.0.jar app.jar

# Set ownership of the application jar
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Expose the port the app runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
