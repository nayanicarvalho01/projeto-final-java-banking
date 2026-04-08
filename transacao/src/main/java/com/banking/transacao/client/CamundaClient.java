package com.banking.transacao.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CamundaClient {

    private final RestTemplate restTemplate;

    @Value("${camunda.bpm.client.base-url:http://localhost:8080/engine-rest}")
    private String camundaUrl;

    public void startProcess(String processKey, Map<String, Object> variables) {
        String url = camundaUrl + "/process-definition/key/" + processKey + "/start";

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> formattedVariables = new HashMap<>();
        variables.forEach((key, value) -> {
            Map<String, Object> variable = new HashMap<>();
            variable.put("value", value);
            variable.put("type", getType(value));
            formattedVariables.put(key, variable);
        });

        request.put("variables", formattedVariables);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        log.info("Iniciando processo Camunda - Key: {}, URL: {}", processKey, url);

        try {
            restTemplate.postForEntity(url, entity, Map.class);
            log.info("Processo Camunda iniciado com sucesso - Key: {}", processKey);
        } catch (Exception e) {
            log.error("Erro ao iniciar processo Camunda - Key: {}", processKey, e);
            throw new RuntimeException("Falha ao iniciar processo Camunda", e);
        }
    }

    private String getType(Object value) {
        if (value instanceof String) return "String";
        if (value instanceof Integer || value instanceof Long) return "Long";
        if (value instanceof Double || value instanceof java.math.BigDecimal) return "Double";
        if (value instanceof Boolean) return "Boolean";
        return "String";
    }
}