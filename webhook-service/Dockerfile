FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy webhook-api sources
COPY ./webhook-service/settings.gradle.kts ./settings.gradle.kts
COPY ./webhook-service/src ./src
COPY ./webhook-service/openapi ./openapi
COPY ./webhook-service/build.gradle.kts ./build.gradle.kts
COPY ./webhook-service/.env ./.env

#Build project
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./webhook-service.jar

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "webhook-service.jar", "--spring.profiles.active=prod"]