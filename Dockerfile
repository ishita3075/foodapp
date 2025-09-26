# ------------ Build Stage ------------
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml and download dependencies first (for caching)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the rest of the source
COPY src src

# Build the JAR
RUN ./mvnw clean package -DskipTests


# ------------ Runtime Stage ------------
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/foodapp-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
