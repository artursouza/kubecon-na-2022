# build stage build the jar with all our resources
FROM maven:3-openjdk-11 as build

VOLUME /tmp
WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

ADD src/ /build/src/
RUN mvn package

# package stage
FROM openjdk:11-jre-slim

ARG JAR_FILE
COPY --from=build /build/target/dapr-discord-binding-*-jar-with-dependencies.jar /opt/dapr-discord-binding.jar
WORKDIR /opt/

EXPOSE 3000
ENTRYPOINT java -jar dapr-discord-binding.jar