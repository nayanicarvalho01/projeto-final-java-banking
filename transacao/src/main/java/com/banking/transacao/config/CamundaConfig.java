package com.banking.transacao.config;

import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CamundaConfig {

    @Bean
    public ExternalTaskClient externalTaskClient() {

        return ExternalTaskClient.create()
                .baseUrl("http://camunda:8080/engine-rest")
                .asyncResponseTimeout(1000)
                .build();
    }
}
