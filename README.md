# 🏦 Banking Microservices com Observabilidade (New Relic)

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=java&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5_&_4.0.5-6DB33F?style=flat-square&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?style=flat-square&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![Camunda](https://img.shields.io/badge/Camunda_BPM-Orchestration-orange?style=flat-square)](https://camunda.com/)
[![New Relic](https://img.shields.io/badge/New_Relic-Observability-008C99?style=flat-square&logo=newrelic&logoColor=white)](https://newrelic.com/)

Um sistema bancário distribuído construído com arquitetura de microserviços, orquestração de processos BPMN e mensageria assíncrona. O grande diferencial deste projeto é a implementação de **Observabilidade Full-Stack (End-to-End)** utilizando o New Relic via Docker.

---

## 🏗️ Arquitetura do Sistema

O sistema é composto por 5 microserviços independentes que se comunicam de forma síncrona (REST) e assíncrona (Kafka), orquestrados pelo Camunda BPM.

*   **Frontend (`:8000`):** Interface web construída com Spring Boot e Thymeleaf. Recebe notificações em tempo real via SSE.
*   **Serviço de Saldo (`:8081`):** Responsável pela validação e atualização de contas. Utiliza Redis para garantir atomicidade nas transações (evitando concorrência).
*   **Serviço de Transação (`:8082`):** Core da aplicação. Valida o saldo via REST e, se aprovado, inicia o fluxo de orquestração no Camunda.
*   **Serviço de Notificação (`:8083`):** Consumidor Kafka. Escuta os tópicos de aprovação/negação e dispara alertas em tempo real (Server-Sent Events) para o Frontend.
*   **Serviço Extrato/Fatura (`:8084`):** Executa os *External Task Workers* do Camunda. Atualiza o banco de dados e gera comprovantes em PDF.

### ⚡ Fluxo de "Fast-Fail" (Transações Negadas)
Para otimizar recursos, caso o serviço de Saldo negue a operação (saldo insuficiente), a transação **não é persistida no banco de dados** e o fluxo do Camunda **não é iniciado**. O sistema publica imediatamente um evento de falha no Kafka, notificando o usuário em milissegundos.

---

## 📊 Observabilidade (New Relic)

O ambiente foi instrumentado para fornecer visibilidade total em tempo real:
*   **APM (Application Performance Monitoring):** Métricas de Throughput, Response Time (p95) e Error Rate de todos os serviços Spring Boot.
*   **Distributed Tracing:** Rastreamento de uma transação de ponta a ponta, correlacionando chamadas REST, mensagens do Kafka e execuções de background do Camunda em uma única linha do tempo.
*   **Infrastructure Monitoring:** Coleta de métricas de CPU, RAM e Rede dos containers Docker.
*   **Centralized Logging:** Logs da aplicação exportados diretamente para a nuvem.

---

## 🛠️ Tecnologias Utilizadas

*   **Linguagem & Framework:** Java 21, Spring Boot (Web, Data MongoDB, Data Redis)
*   **Mensageria:** Apache Kafka & Zookeeper
*   **Banco de Dados:** MongoDB (Persistência) e Redis (Cache/Atomicidade)
*   **Orquestração:** Camunda BPM Platform (Standalone / External Tasks)
*   **Infraestrutura:** Docker & Docker Compose
*   **Monitoramento:** New Relic (Java Agent & Infrastructure Agent)
*   **Outros:** iText7 (Geração de PDFs), Lombok, Thymeleaf

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
*   Docker e Docker Compose instalados.
*   Java 21 (JDK) instalado localmente.
*   Uma conta gratuita no [New Relic](https://newrelic.com/) para obter a License Key.

### 1. Configuração do Ambiente
Crie um arquivo `.env` na raiz do projeto e adicione sua chave do New Relic:
```env
NEW_RELIC_LICENSE_KEY=sua_chave_aqui

2. Build dos Microserviços (Local)
Para evitar problemas de certificado em redes corporativas durante o build do Docker, gere os arquivos .jar localmente executando o script abaixo na raiz do projeto:

bash

Copiar código
for d in saldo transacao notificacao extrato-fatura frontend; do
    echo "Compilando $d..."
    cd $d
    ./gradlew clean bootJar -x test
    cd ..
done

3. Subir a Infraestrutura (Docker)
Execute o Docker Compose para criar as imagens e subir todos os containers (Bancos, Kafka, Camunda e Microserviços):

bash

Copiar código
docker compose up -d --build

(Verifique se todos os serviços estão Healthy rodando docker compose ps)

4. Deploy do Processo BPMN (Camunda)
Com a infraestrutura rodando, publique o fluxo de trabalho no motor do Camunda:

bash

Copiar código
curl -X POST http://localhost:8080/engine-rest/deployment/create \
  -F "deployment-name=processo-transacao" \
  -F "processo-transacao.bpmn=@transacao/src/main/resources/bpmn/processo-transacao.bpmn"

5. Acessar a Aplicação
Frontend (Simulador): http://localhost:8000
Camunda Cockpit (Admin): http://localhost:8080/camunda
Redis Insight (UI): http://localhost:5540
👨‍💻 Autor
Desenvolvido por Nayani Carvalho.
Projeto criado para fins de estudo e demonstração de arquiteturas modernas, microsserviços e observabilidade avançada.
