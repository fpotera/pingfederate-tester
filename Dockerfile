FROM eclipse-temurin:17-jdk-jammy AS java-test

ARG token
ENV CODEARTIFACT_AUTH_TOKEN $token

WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY code ./code
COPY test ./test
COPY maven/settings.xml /root/.m2/

RUN ./mvnw dependency:resolve

CMD ["./mvnw", "verify"]
