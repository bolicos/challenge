# ====== BUILD ======
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle ./
COPY settings.gradle ./

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon


# ====== RUNTIME ======
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]



