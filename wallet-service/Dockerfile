FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy wallet-service sources
COPY ./wallet-service/settings.gradle.kts ./settings.gradle.kts
COPY ./wallet-service/src ./src
COPY ./wallet-service/openapi ./openapi
COPY ./wallet-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./wallet-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_HOST=wallet-service-db
ENV DB_PORT=5432
ENV DB_NAME=wallet_service_db

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "wallet-service.jar"]