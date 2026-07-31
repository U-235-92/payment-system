FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy currency-rate-service sources
COPY ./payment-provider-service/settings.gradle.kts ./settings.gradle.kts
COPY ./payment-provider-service/src ./src
COPY ./payment-provider-service/openapi ./openapi
COPY ./payment-provider-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./payment-provider-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=payment-provider-service-db
ENV DB_PORT=5432
ENV DB_NAME=payment_provider_service_db

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "payment-provider-service.jar"]