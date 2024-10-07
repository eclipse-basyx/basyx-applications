# Stage 1: Build the application
FROM maven:3.8.1-openjdk-17-slim AS build
WORKDIR /workspace
# Copy the pom.xml file and download the dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy the rest of the source code
COPY src src
# Build the application
RUN mvn package -DskipTests

# Stage 2: Run the application
FROM openjdk:17-jdk-slim
# create application directory
WORKDIR /application
# Copy the jar file from the build stage
COPY --from=build /workspace/target/dashboard.jar app.jar
COPY src/main/resources/application.yml application.yml
# Run the Application
ENTRYPOINT ["java" , "-jar" , "app.jar"]
# Make port 8085 available to the world outside this container
EXPOSE 8085
