FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy individuals-api sources
COPY ./individuals-api/settings.gradle.kts ./settings.gradle.kts
COPY ./individuals-api/src ./src
COPY ./individuals-api/openapi ./openapi
COPY ./individuals-api/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./individuals-api.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "individuals-api.jar"]