FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy currency-rate-service sources
COPY ./currency-rate-service/settings.gradle.kts ./settings.gradle.kts
COPY ./currency-rate-service/src ./src
COPY ./currency-rate-service/openapi ./openapi
COPY ./currency-rate-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./currency-rate-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=currency-rate-service-db
ENV DB_PORT=5432
ENV DB_NAME=currency_rate_service_db

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "currency-rate-service.jar"]