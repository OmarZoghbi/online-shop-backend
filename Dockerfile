# ---- Build Stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle Wrapper + Source Code kopieren
COPY . .

# Build ohne Tests
RUN ./gradlew build -x test

# ---- Run Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Die fertige JAR ins Image kopieren
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
