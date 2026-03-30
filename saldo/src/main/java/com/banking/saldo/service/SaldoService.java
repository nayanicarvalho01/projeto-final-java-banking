package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.Tipo;
import com.banking.saldo.model.dto.SaldoResponseDTO;
import com.banking.saldo.repository.SaldoRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SaldoService {

    private final SaldoRepository repository;
    private final SaldoCacheRedisService cache;
    private final RedisLockService lockService;

    private static final String LOCK_PREFIX = "lock:saldo:";

    public SaldoService(SaldoRepository repository,
                        SaldoCacheRedisService cache,
                        RedisLockService lockService) {
        this.repository = repository;
        this.cache = cache;
        this.lockService = lockService;
    }

    // ================================
    // 🔹 BUSCA (CACHE FIRST)
    // ================================
    public Saldo buscarSaldo(String contaId) {

        Saldo saldo = cache.get(contaId);

        if (saldo != null) {
            return saldo;
        }

        saldo = repository.findById(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        cache.save(saldo);

        return saldo;
    }

    // ================================
    // 🔥 ATUALIZAÇÃO COM CONCORRÊNCIA
    // ================================
    public void atualizar(String contaId, BigDecimal valor, Tipo tipo) {

        String lockKey = LOCK_PREFIX + contaId;

        boolean locked = lockService.lock(lockKey, 5000);

        if (!locked) {
            throw new RuntimeException("Conta em processamento, tente novamente");
        }

        try {

            Saldo saldo = buscarSaldo(contaId);

            if (tipo == Tipo.DEBITO) {

                if (saldo.getSaldoDebito().compareTo(valor) < 0) {
                    throw new RuntimeException("Saldo insuficiente");
                }

                saldo.setSaldoDebito(
                        saldo.getSaldoDebito().subtract(valor)
                );

            } else if (tipo == Tipo.CREDITO) {

                if (saldo.getLimiteCredito().compareTo(valor) < 0) {
                    throw new RuntimeException("Limite insuficiente");
                }

                saldo.setLimiteCredito(
                        saldo.getLimiteCredito().subtract(valor)
                );

            } else {
                throw new RuntimeException("Tipo inválido");
            }

            // ordem correta (OBRIGATÓRIA)
            repository.save(saldo);
            cache.save(saldo);

        } finally {
            lockService.unlock(lockKey);
        }
    }
}