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

    @Value("${camunda.bpm.client.async-response-timeout:2000}")
    private long asyncResponseTimeout;

    @Value("${camunda.bpm.client.lock-duration:60000}")
    private long lockDuration;


    @Bean
    public ExternalTaskClient externalTaskClient() {
        return ExternalTaskClient.create()
                .baseUrl(camundaUrl)
                .workerId(workerId)
                .maxTasks(maxTasks)
                .asyncResponseTimeout(asyncResponseTimeout)
                .lockDuration(lockDuration)
                .build();
    }
}
