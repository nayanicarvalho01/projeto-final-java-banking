package com.banking.transacao.service;

import com.banking.transacao.client.SaldoClient;
import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final SaldoClient saldoClient;

    public Transacao processar(TransacaoRequest request) {
        log.info("Processando transação - ContaId: {}, Valor: {}, Tipo: {}",
                request.getContaId(), request.getValor(), request.getTipo());

        try {
            saldoClient.atualizarSaldo(
                    request.getContaId(),
                    request.getValor(),
                    request.getTipo().toTipoSaldo()
            );

            Transacao transacao = Transacao.builder()
                    .id(UUID.randomUUID().toString())
                    .contaId(request.getContaId())
                    .comerciante(request.getComerciante())
                    .localizacao(request.getLocalizacao())
                    .valor(request.getValor())
                    .tipo(request.getTipo())
                    .status(Status.APROVADA)
                    .dataHora(Instant.now())
                    .build();

            return transacaoRepository.save(transacao);

        } catch (Exception e) {

            log.warn("Transação negada - ContaId: {}, Motivo: {}",
                    request.getContaId(), e.getMessage());

            return Transacao.builder()
                    .id(UUID.randomUUID().toString())
                    .contaId(request.getContaId())
                    .comerciante(request.getComerciante())
                    .localizacao(request.getLocalizacao())
                    .valor(request.getValor())
                    .tipo(request.getTipo())
                    .status(Status.NEGADA)
                    .dataHora(Instant.now())
                    .build();
        }
    }

    public List<Transacao> buscarPorConta(String contaId) {
        log.info("Buscando transações da conta: {}", contaId);
        return transacaoRepository.findByContaIdOrderByDataHoraDesc(contaId);
    }
}