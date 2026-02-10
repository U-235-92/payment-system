FROM gradle:jdk25-alpine AS builder
WORKDIR /source
COPY ./individuals-api/build.gradle.kts ./individuals-api/build.gradle.kts
COPY ./settings.gradle.kts ./settings.gradle.kts
COPY ./individuals-api/src ./individuals-api/src
COPY ./individuals-api/openapi ./individuals-api/openapi
WORKDIR /source
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y curl
ENV APPLICATION_KEYCLOAK_PORT=8080
ENV APPLICATION_KEYCLOAK_SERVER_URI=http://keycloak:${APPLICATION_KEYCLOAK_PORT}
WORKDIR /app
COPY --from=builder /source/individuals-api/build/libs/*.jar ./individuals-api.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "individuals-api.jar"]