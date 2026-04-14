package com.banking.frontend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.YearMonth;

@Slf4j
@Service
public class ExtratoFaturaClientService {

    @Value("${services.extrato-fatura.url}")
    private String extratoFaturaServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public byte[] gerarExtratoPdf(String contaId, YearMonth mes) {
        try {
            String url = String.format("%s/api/extratos/%s/pdf?mes=%s",
                    extratoFaturaServiceUrl, contaId, mes.toString());

            log.info("Gerando extrato PDF: {}", url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao gerar extrato PDF", e);
            return null;
        }
    }

    public byte[] gerarFaturaPdf(String contaId, YearMonth mes) {
        try {
            String url = String.format("%s/api/faturas/%s/pdf?mes=%s",
                    extratoFaturaServiceUrl, contaId, mes.toString());

            log.info("Gerando fatura PDF: {}", url);

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            return response.getBody();
        } catch (Exception e) {
            log.error("Erro ao gerar fatura PDF", e);
            return null;
        }
    }
}