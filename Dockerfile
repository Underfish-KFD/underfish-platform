# ---- Build stage ----
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

# Копируем корневой pom + модуль
COPY pom.xml .
COPY underfish/pom.xml underfish/pom.xml
COPY underfish/src underfish/src

RUN apt-get update && apt-get install -y maven && \
    mvn -P docker package -DskipTests --no-transfer-progress

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Jar теперь в underfish/target/
COPY --from=builder /build/underfish/target/underfish-app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
