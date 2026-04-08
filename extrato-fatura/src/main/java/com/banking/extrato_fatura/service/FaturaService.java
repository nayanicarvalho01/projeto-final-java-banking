package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.model.ItemFatura;
import com.banking.extrato_fatura.repository.FaturaRepository;
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
public class FaturaService {

    private final FaturaRepository faturaRepository;

    public void adicionarItem(String contaId, String transacaoId, String comerciante,
                              String localizacao, BigDecimal valor, Instant dataHora) {

        YearMonth mesAtual = YearMonth.now();

        Fatura fatura = faturaRepository
                .findByContaIdAndMesReferencia(contaId, mesAtual)
                .orElse(Fatura.builder()
                        .contaId(contaId)
                        .mesReferencia(mesAtual)
                        .valorTotal(BigDecimal.ZERO)
                        .dataVencimento(mesAtual.atEndOfMonth().plusDays(10))
                        .build());

        ItemFatura item = ItemFatura.builder()
                .transacaoId(transacaoId)
                .comerciante(comerciante)
                .localizacao(localizacao)
                .valor(valor)
                .dataHora(dataHora)
                .build();

        fatura.getItens().add(item);

        BigDecimal novoTotal = fatura.getItens().stream()
                .map(ItemFatura::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        fatura.setValorTotal(novoTotal);

        faturaRepository.save(fatura);

        log.info("Item adicionado à fatura - Conta: {}, Mês: {}, Transação: {}, Total: {}",
                contaId, mesAtual, transacaoId, novoTotal);
    }

    public Fatura buscarPorMes(String contaId, YearMonth mesReferencia) {
        return faturaRepository
                .findByContaIdAndMesReferencia(contaId, mesReferencia)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada"));
    }

    public List<Fatura> buscarPorPeriodo(String contaId, YearMonth inicio, YearMonth fim) {
        return faturaRepository.findByContaIdAndMesReferenciaBetween(contaId, inicio, fim);
    }

    public List<Fatura> buscarPorStatus(String contaId, StatusFatura status) {
        return faturaRepository.findByContaIdAndStatus(contaId, status);
    }

    public List<Fatura> buscarTodas(String contaId) {
        return faturaRepository.findByContaIdOrderByMesReferenciaDesc(contaId);
    }

    public void fecharFatura(String faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada"));

        fatura.setStatus(StatusFatura.FECHADA);
        faturaRepository.save(fatura);

        log.info("Fatura fechada - ID: {}, Conta: {}, Total: {}",
                faturaId, fatura.getContaId(), fatura.getValorTotal());
    }

    public void pagarFatura(String faturaId) {
        Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada"));

        if (fatura.getStatus() != StatusFatura.FECHADA) {
            throw new RuntimeException("Apenas faturas FECHADAS podem ser pagas");
        }

        fatura.setStatus(StatusFatura.PAGA);
        faturaRepository.save(fatura);

        log.info("Fatura paga - ID: {}, Conta: {}, Total: {}",
                faturaId, fatura.getContaId(), fatura.getValorTotal());
    }
}