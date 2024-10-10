# First stage: Build the project
FROM maven:3.8.1-openjdk-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
RUN ls /app/opc2aas/target/
RUN ls /app/opc2aas/src/main/resources



# Second stage: Run the application
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/opc2aas/target/basyx.opc2aas-3.1.4.jar application.jar
EXPOSE 8081
CMD ["java", "-jar", "application.jar"]
