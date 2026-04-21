# Stage 1: Build the JAR
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/libratrack-1.0-SNAPSHOT.jar app.jar
COPY src/main/resources/schema.sql schema.sql
EXPOSE 7070
ENTRYPOINT ["java", "-cp", "app.jar", "com.libratrack.api.ApiServer"]
