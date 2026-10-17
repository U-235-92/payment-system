FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy payment-service sources
COPY ./payment-service/settings.gradle.kts ./settings.gradle.kts
COPY ./payment-service/src ./src
COPY ./payment-service/openapi ./openapi
COPY ./payment-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./payment-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=payment-service-db
ENV DB_PORT=5432
ENV DB_NAME=payment_service_db

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "payment-service.jar"]