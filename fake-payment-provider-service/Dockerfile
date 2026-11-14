FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy currency-rate-service sources
COPY ./fake-payment-provider-service/settings.gradle.kts ./settings.gradle.kts
COPY ./fake-payment-provider-service/src ./src
COPY ./fake-payment-provider-service/openapi ./openapi
COPY ./fake-payment-provider-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./fake-payment-provider-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=fake-payment-provider-service-db
ENV DB_PORT=5432
ENV DB_NAME=fake_payment_provider_service_db

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "fake-payment-provider-service.jar"]