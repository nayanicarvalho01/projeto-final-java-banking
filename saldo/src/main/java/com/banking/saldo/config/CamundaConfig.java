package com.banking.saldo.config;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CamundaConfig {

    @Value("${camunda.bpm.client.base-url:http://localhost:8080/engine-rest}")
    private String camundaUrl;

    @Bean
    public ExternalTaskClient externalTaskClient() {
        log.info("Conectando ao Camunda: {}", camundaUrl);

        return ExternalTaskClient.create()
                .baseUrl(camundaUrl)
                .workerId("saldo-service")
                .asyncResponseTimeout(10000)
                .maxTasks(10)
                .lockDuration(20000)
                .build();
    }
}