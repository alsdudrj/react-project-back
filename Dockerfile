# 1. build stage
FROM eclipse-temurin:17-jdk-alpine AS build
COPY . .
RUN ./gradlew build -x test

# 2. start stage
FROM eclipse-temurin:17-jre-alpine
COPY --from=build /build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]