package com.banking.frontend.service;

import com.banking.frontend.dto.TransacaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TransacaoClientService {

    @Value("${services.transacao.url}")
    private String transacaoServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<TransacaoResponse> buscarTransacoes(String contaId) {
        try {
            String url = transacaoServiceUrl + "/api/transacoes/conta/" + contaId;
            log.info("Buscando transações: {}", url);

            return restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TransacaoResponse>>() {}
            ).getBody();
        } catch (Exception e) {
            log.error("Erro ao buscar transações", e);
            return Collections.emptyList();
        }
    }
}