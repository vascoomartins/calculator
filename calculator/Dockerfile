FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./

COPY messaging ./messaging
COPY calculator ./calculator
COPY rest ./rest

RUN mvn clean package -DskipTests -pl calculator -am

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/calculator/target/calculator-*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]