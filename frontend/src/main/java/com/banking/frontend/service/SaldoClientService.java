package com.banking.frontend.service;


import com.banking.frontend.dto.SaldoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SaldoClientService {

    @Value("${services.saldo.url}")
    private String saldoServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public SaldoResponse buscarSaldo(String contaId) {
        try {
            String url = saldoServiceUrl + "/api/saldos/" + contaId;
            log.info("Buscando saldo: {}", url);

            return restTemplate.getForObject(url, SaldoResponse.class);
        } catch (Exception e) {
            log.error("Erro ao buscar saldo", e);
            return null;
        }
    }
}