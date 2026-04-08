package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.model.ItemExtrato;
import com.banking.extrato_fatura.repository.ExtratoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtratoService {

    private final ExtratoRepository extratoRepository;

    public void adicionarItem(String contaId, String transacaoId, String comerciante,
                              String localizacao, BigDecimal valor, Instant dataHora) {

        YearMonth mesAtual = YearMonth.now();

        Extrato extrato = extratoRepository
                .findByContaIdAndMesReferencia(contaId, mesAtual)
                .orElse(Extrato.builder()
                        .contaId(contaId)
                        .mesReferencia(mesAtual)
                        .build());

        ItemExtrato item = ItemExtrato.builder()
                .transacaoId(transacaoId)
                .comerciante(comerciante)
                .localizacao(localizacao)
                .valor(valor)
                .dataHora(dataHora)
                .build();

        extrato.getItens().add(item);

        extratoRepository.save(extrato);

        log.info("Item adicionado ao extrato - Conta: {}, Mês: {}, Transação: {}",
                contaId, mesAtual, transacaoId);
    }

    public Extrato buscarPorMes(String contaId, YearMonth mesReferencia) {
        return extratoRepository
                .findByContaIdAndMesReferencia(contaId, mesReferencia)
                .orElseThrow(() -> new RuntimeException("Extrato não encontrado"));
    }

    public List<Extrato> buscarPorPeriodo(String contaId, YearMonth inicio, YearMonth fim) {
        return extratoRepository.findByContaIdAndMesReferenciaBetween(contaId, inicio, fim);
    }

    public List<Extrato> buscarTodos(String contaId) {
        return extratoRepository.findByContaIdOrderByMesReferenciaDesc(contaId);
    }
}