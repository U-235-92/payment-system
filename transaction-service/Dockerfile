FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy transaction-api sources
COPY ./transaction-service/settings.gradle.kts ./settings.gradle.kts
COPY ./transaction-service/src ./src
COPY ./transaction-service/openapi ./openapi
COPY ./transaction-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./transaction-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "transaction-service.jar"]