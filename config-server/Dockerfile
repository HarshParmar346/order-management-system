# Build context = repository root (the "evenflow" folder)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY common-events common-events
COPY config-server config-server
COPY order-service order-service
COPY inventory-service inventory-service
COPY payment-service payment-service
COPY notification-service notification-service

RUN mvn -q -pl config-server -am -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/config-server/target/config-server-*.jar app.jar
# Bake the configurations repo into the image so it doesn't depend on your host filesystem
COPY configurations /config-repo

EXPOSE 9690
ENTRYPOINT ["java", "-jar", "app.jar"]