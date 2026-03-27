FROM eclipse-temurin:25-jre
WORKDIR /app
COPY ../../build/libs/*.jar ./individuals-api.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://keycloak:8080/realms/payment-system
ENV SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/certs
ENV SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_ISSUER_URI=http://keycloak:8080/realms/payment-system
ENV SPRING_SECURITY_OAUTH2_CLIENT_PROVIDER_KEYCLOAK_TOKEN_URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/token
ENV SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_ID=individuals-api
ENV SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KEYCLOAK_CLIENT_SECRET=qomP5PU6JMurRIXkbFpQP0kbhqqA6RT5

#Keycloak env:
ENV KEYCLOAK_ADMIN_CLIENT_ID=payment-system-admin-cli
ENV KEYCLOAK_ADMIN_CLIENT_SECRET=RGybolPwVl7FMtZDyZCn6owhkY3ewtam
ENV KEYCLOAK_ADMIN_URI=http://keycloak:8080/admin/realms/payment-system/users

#External project services env:
ENV APPLICATION_PERSON_SERVICE_URI=http://person-service:8082

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "individuals-api.jar"]