FROM gradle:jdk25-alpine AS builder
WORKDIR /source

#Copy settings of root project [payment-system]
COPY ./settings.gradle.kts ./settings.gradle.kts

#Copy modules of root project
#Copy individuals-api module (without this module build fail. E.g. [./settings.gradle.kts] include this module)
COPY ./individuals-api/src ./individuals-api/src
COPY ./individuals-api/openapi ./individuals-api/openapi
COPY ./individuals-api/build.gradle.kts ./individuals-api/build.gradle.kts

#Copy person-service module
COPY ./person-service/src ./person-service/src
COPY ./person-service/openapi ./person-service/openapi
COPY ./person-service/build.gradle.kts ./person-service/build.gradle.kts

#Build individuals-api module
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY --from=builder /source/person-service/build/libs/*.jar ./person-service.jar

#Using [\] to make less layers
#Spring env:
ENV SPRING_PROFILES_ACTIVE=dev \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://keycloak:8080/realms/payment-system \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/certs

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "person-service.jar"]