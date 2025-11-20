FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /opt/app

COPY --from=build /workspace/target/GuitarMatchIS-0.0.1.jar /opt/app/app.jar
COPY docker/entrypoint.sh /opt/app/entrypoint.sh
RUN chmod +x /opt/app/entrypoint.sh

EXPOSE 5252
ENV PORT=5252

ENTRYPOINT ["/opt/app/entrypoint.sh"]


