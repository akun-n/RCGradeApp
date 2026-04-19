FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy project files and build executable jar.
COPY . .
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar produced in build stage.
COPY --from=build /app/target/rcgrade-app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
