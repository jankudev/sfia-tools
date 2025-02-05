# build the app
FROM gradle:8.12.1-jdk21-alpine AS builder

WORKDIR /app
COPY . .
RUN gradle clean :api-service:build -x test

# run the app
FROM openjdk:21-slim

WORKDIR /app
COPY --from=builder /app/api-service/build/libs/api-service-*.jar app.jar
COPY ../api-service/src/main/resources/config/application.yml application.yml

VOLUME /app/data
EXPOSE 80
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=application.yml"]