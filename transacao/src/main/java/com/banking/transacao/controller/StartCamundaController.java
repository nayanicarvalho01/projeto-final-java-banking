package com.banking.transacao.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/camunda")
@AllArgsConstructor
public class StartCamundaController {

    private final RestTemplate restTemplate;

    private final String CAMUNDA_URL = "http://localhost:8080/engine-rest/message";

    @GetMapping
    @RequestMapping("/start")
    public void start() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("messageName", "iniciarTransacao");
        payload.put("resultEnabled", true);
        payload.put("variablesInResultEnabled", true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String response = restTemplate.postForObject(CAMUNDA_URL, request, String.class);

        System.out.println(response);
    }

}
