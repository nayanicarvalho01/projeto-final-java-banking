package com.banking.extrato_fatura.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Value("${camunda.bpm.client.base-url}")
    private String camundaUrl;

    @Value("${camunda.bpm.client.worker-id}")
    private String workerId;

    @Value("${camunda.bpm.client.max-tasks:10}")
    private int maxTasks;

    @Bean
    public ExternalTaskClient externalTaskClient() {
        return ExternalTaskClient.create()
                .baseUrl(camundaUrl)
                .workerId(workerId)
                .maxTasks(maxTasks)
                .build();
    }
}
