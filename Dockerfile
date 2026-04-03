# Build Stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# The JAR file is built outside the Docker build process (e.g. by Render or local Maven)
# and then copied into the image.
COPY target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application with optimized JVM flags
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
