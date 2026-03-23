#FROM gradle:jdk25-alpine AS builder
#WORKDIR /source
#COPY ./buildSrc ./buildSrc
#COPY ./common-configs ./common-configs
#COPY ./common-dto ./common-dto
#COPY ./person-service/build.gradle.kts ./person-service/build.gradle.kts
#COPY ./person-service/src ./person-service/src
#COPY ./settings.gradle.kts ./settings.gradle.kts
#COPY ./build.gradle.kts ./build.gradle.kts
#RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY ./build/libs/*.jar ./person-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "person-service.jar"]