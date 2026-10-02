# Multi-stage build for the URL Shortener
FROM maven:3.8.8-eclipse-temurin-8 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -q -e -DskipTests package

FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

ENV JAVA_OPTS=""
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
