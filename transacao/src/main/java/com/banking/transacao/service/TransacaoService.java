package com.banking.transacao.service;

import com.banking.transacao.client.CamundaClient;
import com.banking.transacao.client.SaldoClient;
import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final SaldoClient saldoClient;
    private final CamundaClient camundaClient;

    public Transacao processar(TransacaoRequest request) {
        log.info("Processando transação - Conta: {}, Valor: {}, Tipo: {}",
                request.getContaId(), request.getValor(), request.getTipo());

        try {
            saldoClient.atualizarSaldo(
                    request.getContaId(),
                    request.getValor(),
                    request.getTipo().toTipoSaldo()
            );

            Transacao transacao = construirTransacao(request, Status.APROVADA);
            transacao = transacaoRepository.save(transacao);

            iniciarProcessoCamunda(transacao);

            return transacao;

        } catch (Exception e) {
            log.warn("Transação negada - Conta: {}, Motivo: {}",
                    request.getContaId(), e.getMessage());

            return construirTransacao(request, Status.NEGADA);
        }
    }

    private void iniciarProcessoCamunda(Transacao transacao) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("transacaoId", transacao.getId());
            variables.put("contaId", transacao.getContaId());
            variables.put("comerciante", transacao.getComerciante());
            variables.put("localizacao", transacao.getLocalizacao());
            variables.put("valor", transacao.getValor());
            variables.put("tipo", transacao.getTipo().name());
            variables.put("status", transacao.getStatus().name());

            camundaClient.startProcess("processo-transacao", variables);

            log.info("Processo Camunda iniciado - Transação: {}", transacao.getId());

        } catch (Exception e) {
            log.error("Erro ao iniciar Camunda - Transação: {}", transacao.getId(), e);
        }
    }

    public List<Transacao> buscarPorConta(String contaId) {
        log.info("Buscando transações da conta: {}", contaId);
        return transacaoRepository.findByContaIdOrderByDataHoraDesc(contaId);
    }

    private Transacao construirTransacao(TransacaoRequest request, Status status) {
        return Transacao.builder()
                .contaId(request.getContaId())
                .comerciante(request.getComerciante())
                .localizacao(request.getLocalizacao())
                .valor(request.getValor())
                .tipo(request.getTipo())
                .status(status)
                .dataHora(Instant.now())
                .build();
    }
}