FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="banking-team"
LABEL service="servico-saldo"

RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

RUN mkdir -p /newrelic /app/logs && chown -R spring:spring /app /newrelic

# Copia o JAR que você vai gerar na sua máquina direto para a imagem
COPY build/libs/*.jar app.jar

USER spring
EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]