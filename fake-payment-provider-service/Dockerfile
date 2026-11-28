FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy fake-payment-provider-service sources
COPY ./fake-payment-provider-service/settings.gradle.kts ./settings.gradle.kts
COPY ./fake-payment-provider-service/src ./src
COPY ./fake-payment-provider-service/openapi ./openapi
COPY ./fake-payment-provider-service/build.gradle.kts ./build.gradle.kts
COPY ./fake-payment-provider-service/dev.env ./.env

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./fake-payment-provider-service.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "fake-payment-provider-service.jar", "--spring.profiles.active=prod"]