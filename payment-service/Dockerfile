FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy payment-service sources
COPY ./payment-service/settings.gradle.kts ./settings.gradle.kts
COPY ./payment-service/src ./src
COPY ./payment-service/openapi ./openapi
COPY ./payment-service/build.gradle.kts ./build.gradle.kts
COPY ./payment-service/dev.env ./.env

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./payment-service.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "payment-service.jar", "--spring.profiles.active=prod"]