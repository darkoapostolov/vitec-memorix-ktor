FROM gradle:8.3.0-jdk17 as builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts gradlew gradle.properties ./
COPY gradle gradle
COPY src ./src

RUN chmod +x gradlew

RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
