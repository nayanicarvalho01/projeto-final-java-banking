FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="banking-team"
LABEL service="frontend-service"

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Criar diretórios necessários
RUN mkdir -p /newrelic /app/logs && chown -R spring:spring /app /newrelic

# Copiar JAR gerado na sua máquina
COPY build/libs/*.jar app.jar

# Mudar para usuário não-root
USER spring
EXPOSE 8000

ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]