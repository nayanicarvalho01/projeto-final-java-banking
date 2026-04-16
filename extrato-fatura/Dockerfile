FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="banking-team"
LABEL service="servico-extrato-fatura"

# Instalar fontes para geração de PDF
RUN apk add --no-cache fontconfig ttf-dejavu && fc-cache -f

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Criar diretórios necessários
RUN mkdir -p /newrelic /app/logs /app/pdfs && chown -R spring:spring /app /newrelic

# Copiar JAR gerado na sua máquina (WSL)
COPY build/libs/*.jar app.jar

# Mudar para usuário não-root
USER spring
EXPOSE 8084

ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]