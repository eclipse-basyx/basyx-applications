# First stage: Build the project
FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests



# Second stage: Run the application
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/target/TestOrchestrator.jar application.jar
CMD ["java", "-jar", "application.jar"]
