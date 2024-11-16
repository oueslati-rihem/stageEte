# Step 1: Build the application
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Create the final image
FROM openjdk:17-jdk-slim
EXPOSE 8089
COPY --from=build /app/target/gestion-station-ski-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
