FROM gradle:jdk25-alpine AS builder

WORKDIR /source

#Copy database-migration-service sources
COPY ./database-migration-service/settings.gradle.kts ./settings.gradle.kts
COPY ./database-migration-service/src ./src
COPY ./database-migration-service/build.gradle.kts ./build.gradle.kts

#Build project
RUN gradle --no-daemon clean bootJar -x test

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y curl

WORKDIR /app

COPY --from=builder /source/build/libs/*.jar ./database-migration-service.jar

EXPOSE 18581
ENTRYPOINT ["java", "-jar", "database-migration-service.jar"]