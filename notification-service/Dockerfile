# Build context must be the repository root (needed for the multi-module Maven reactor)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common-events common-events
COPY config-server config-server
COPY notification-service notification-service
COPY order-service order-service
COPY inventory-service inventory-service
COPY payment-service payment-service

RUN mvn -q -pl notification-service -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/notification-service/target/notification-service-*.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
