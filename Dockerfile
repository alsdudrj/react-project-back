# 1. 빌드 스테이지
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 빌드에 필요한 파일들만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 실행 권한 부여 및 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2. 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드 스테이지에서 생성된 jar 파일 중 plain이 붙지 않은 실행 가능한 jar만 복사
COPY --from=build /app/build/libs/*[!plain].jar app.jar

# 컨테이너 실행 시 자바 실행
ENTRYPOINT ["java", "-jar", "app.jar"]