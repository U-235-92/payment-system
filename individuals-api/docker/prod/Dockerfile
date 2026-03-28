FROM gradle:jdk25-alpine AS builder
WORKDIR /source

#Copy settings of root project [payment-system]
COPY ./settings.gradle.kts ./settings.gradle.kts

#Copy modules of root project
#Copy individuals-api module
COPY ./individuals-api/build.gradle.kts ./individuals-api/build.gradle.kts
COPY ./individuals-api/src ./individuals-api/src

#Copy person-service module (without this module build fail. E.g. [./settings.gradle.kts] include this module)
COPY ./person-service/src ./person-service/src
COPY ./person-service/build.gradle.kts ./person-service/build.gradle.kts

#Copy common plugins and dependencies of project (see gradle documentation about buildSrc module)
COPY ./buildSrc ./buildSrc

#Copy common OpenApi DTO specification [./common-dto/openapi] + Java DTO [./common-dto/src/main/java]
COPY ./common-dto/src ./common-dto/src
COPY ./common-dto/openapi ./common-dto/openapi
COPY ./common-dto/build.gradle.kts ./common-dto/build.gradle.kts

#Build individuals-api module
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y curl
WORKDIR /app
COPY --from=builder /source/individuals-api/build/libs/*.jar ./individuals-api.jar

#Using [\] to make less layers
#Spring env:
ENV SPRING_PROFILES_ACTIVE=prod \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://keycloak:8080/realms/payment-system \
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/certs \
SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/payment-system \
SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_TOKEN_URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/token \
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_ID=individuals-api \
SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET=qomP5PU6JMurRIXkbFpQP0kbhqqA6RT5 \
MANAGEMENT_OPENTELEMETRY_TRACING_EXPORT_OTLP_ENDPOINT=http://otel-collector:4318/v1/traces

#Keycloak env:
ENV KEYCLOAK_ADMIN_CLIENT_ID=payment-system-admin-cli \
KEYCLOAK_ADMIN_CLIENT_SECRET=RGybolPwVl7FMtZDyZCn6owhkY3ewtam \
KEYCLOAK_ADMIN_URI=http://keycloak:8080/admin/realms/payment-system/users

#External project services env:
ENV APPLICATION_PERSON_SERVICE_URI=http://person-service:8082

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "individuals-api.jar"]