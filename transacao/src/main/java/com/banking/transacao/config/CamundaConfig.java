package com.banking.transacao.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class CamundaConfig {

    @Value("${camunda.bpm.client.base-url:http://localhost:8080/engine-rest}")
    private String camundaUrl;

    @Value("${camunda.bpm.client.worker-id:transacao-worker}")
    private String workerId;

    @Value("${camunda.bpm.client.max-tasks:10}")
    private Integer maxTasks;

    @Value("${camunda.bpm.client.lock-duration:10000}")
    private Long lockDuration;

    @Bean
    public ExternalTaskClient externalTaskClient() {
        log.info("Configurando Camunda External Task Client");
        log.info("URL: {}", camundaUrl);
        log.info("Worker ID: {}", workerId);

        try {
            ExternalTaskClient client = ExternalTaskClient.create()
                    .baseUrl(camundaUrl)
                    .workerId(workerId)
                    .maxTasks(maxTasks)
                    .asyncResponseTimeout(lockDuration)
                    .lockDuration(lockDuration)
                    .build();

            log.info("Camunda External Task Client configurado");
            return client;

        } catch (Exception e) {
            log.error("Erro ao configurar Camunda", e);
            throw new RuntimeException("Falha ao conectar com Camunda: " + e.getMessage(), e);
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        log.info("Configurando RestTemplate");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        return new RestTemplate(factory);
    }
}