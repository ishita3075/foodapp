# Step 1: Use an official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Step 2: Install dos2unix to fix Windows line endings (optional but recommended)
RUN apt-get update && apt-get install -y dos2unix && rm -rf /var/lib/apt/lists/*

# Step 3: Set the working directory inside the container
WORKDIR /app

# Step 4: Copy Maven wrapper and pom.xml (for dependency caching)
COPY mvnw ./
COPY .mvn .mvn
COPY pom.xml ./

# ✅ Fix permissions and convert line endings
RUN dos2unix mvnw && chmod +x mvnw

# Step 5: Pre-download dependencies (this speeds up builds)
RUN ./mvnw dependency:go-offline -B

# Step 6: Copy source code into the container
COPY src src

# Step 7: Build the Spring Boot application JAR
RUN ./mvnw clean package -DskipTests

# Step 8: Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Step 9: Run the JAR file when the container starts
CMD ["java", "-jar", "/app/target/foodapp-0.0.1-SNAPSHOT.jar"]
