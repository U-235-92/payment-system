FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy webhook-api sources
COPY ./webhook-service/settings.gradle.kts ./settings.gradle.kts
COPY ./webhook-service/src ./src
COPY ./webhook-service/openapi ./openapi
COPY ./webhook-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./webhook-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8088
ENTRYPOINT ["java", "-jar", "webhook-service.jar"]