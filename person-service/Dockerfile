FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy person-service sources
COPY ./person-service/settings.gradle.kts ./settings.gradle.kts
COPY ./person-service/src ./src
COPY ./person-service/openapi ./openapi
COPY ./person-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./person-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "person-service.jar"]