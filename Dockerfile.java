FROM docker.io/library/maven:3.9-eclipse-temurin-21-alpine AS build
ARG SERVICE_DIR
WORKDIR /workspace

COPY ${SERVICE_DIR}/pom.xml pom.xml
COPY ${SERVICE_DIR}/src src

RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -DskipTests

FROM docker.io/library/eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl && \
    addgroup -S app && adduser -S app -G app

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
USER app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]