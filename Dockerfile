# Step 1: Use an official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Step 2: Set the working directory inside the container
WORKDIR /app

# Step 3: Copy Maven wrapper and pom.xml (for dependency caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Step 4: Pre-download dependencies (this speeds up builds)
RUN ./mvnw dependency:go-offline -B

# Step 5: Copy source code into the container
COPY src src

# Step 6: Build the Spring Boot application JAR
RUN ./mvnw clean package -DskipTests

# Step 7: Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Step 8: Run the JAR file when the container starts
CMD ["java", "-jar", "target/foodapp-0.0.1-SNAPSHOT.jar"]
