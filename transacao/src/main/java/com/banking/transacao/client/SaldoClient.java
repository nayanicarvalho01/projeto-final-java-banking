package com.banking.transacao.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaldoClient {

    private final RestTemplate restTemplate;

    @Value("${saldo.service.url:http://localhost:8081}")
    private String saldoServiceUrl;

    public void atualizarSaldo(String contaId, BigDecimal valor, String tipo) {
        String url = saldoServiceUrl + "/api/saldos/" + contaId;

        log.info("Atualizando saldo - ContaId: {}, Valor: {}, Tipo: {}",
                contaId, valor, tipo);

        Map<String, Object> request = new HashMap<>();
        request.put("valor", valor);
        request.put("tipo", tipo);

        restTemplate.put(url, request);

        log.info("Saldo atualizado - ContaId: {}", contaId);
    }
}