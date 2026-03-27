FROM eclipse-temurin:25-jre
WORKDIR /app
COPY ../../build/libs/*.jar ./person-service.jar

#Spring env:
ENV SPRING_PROFILES_ACTIVE=dev
ENV SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER-URI=http://keycloak:8080/realms/payment-system
ENV SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK-SET-URI=http://keycloak:8080/realms/payment-system/protocol/openid-connect/certs

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "person-service.jar"]