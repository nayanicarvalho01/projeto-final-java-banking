package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.Tipo;
import com.banking.saldo.repository.SaldoMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaldoService {

    private final SaldoMongoRepository mongoRepository;
    private final SaldoCacheRedisService cacheService;
    private final RedisLockService lockService;

    private static final String LOCK_PREFIX = "lock:conta:";

    public Saldo criarConta(String contaId, BigDecimal saldoInicial, BigDecimal limiteCredito) {
        log.info("Criando conta: {}", contaId);

        if (mongoRepository.existsByContaId(contaId)) {
            throw new RuntimeException("Conta já existe: " + contaId);
        }

        Saldo saldo = Saldo.builder()
                .contaId(contaId)
                .saldoDebito(saldoInicial)
                .limiteCredito(limiteCredito)
                .creditoUtilizado(BigDecimal.ZERO)
                .ultimaAtualizacao(Instant.now())
                .build();

        mongoRepository.save(saldo);
        cacheService.save(saldo);

        log.info("Conta criada: {}", contaId);
        return saldo;
    }

    public Saldo depositar(String contaId, BigDecimal valor) {
        String lockKey = LOCK_PREFIX + contaId;

        if (!lockService.lock(lockKey)) {
            throw new RuntimeException("Conta em processamento");
        }

        try {
            log.info("Depositando {} na conta {}", valor, contaId);

            Saldo saldo = buscarSaldo(contaId);
            saldo.setSaldoDebito(saldo.getSaldoDebito().add(valor));
            saldo.setUltimaAtualizacao(Instant.now());

            mongoRepository.save(saldo);
            cacheService.save(saldo);

            log.info("Depósito realizado: {}", contaId);
            return saldo;

        } catch (Exception e) {
            log.error("Erro ao depositar: {}", e.getMessage());
            cacheService.evict(contaId);
            throw e;
        } finally {
            lockService.unlock(lockKey);
        }
    }

    public Saldo buscarSaldo(String contaId) {
        Saldo saldo = cacheService.get(contaId);

        if (saldo != null) {
            return saldo;
        }

        saldo = mongoRepository.findByContaId(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        cacheService.save(saldo);
        return saldo;
    }

    public Saldo atualizar(String contaId, BigDecimal valor, Tipo tipo) {
        String lockKey = LOCK_PREFIX + contaId;

        if (!lockService.lock(lockKey)) {
            throw new RuntimeException("Conta em processamento");
        }

        try {
            log.info("Atualizando saldo - Conta: {}, Tipo: {}", contaId, tipo);

            Saldo saldo = buscarSaldo(contaId);

            if (tipo == Tipo.DEBITO) {
                processarDebito(saldo, valor);
            } else {
                processarCredito(saldo, valor);
            }

            saldo.setUltimaAtualizacao(Instant.now());
            mongoRepository.save(saldo);
            cacheService.save(saldo);

            log.info("Saldo atualizado: {}", contaId);
            return saldo;

        } catch (Exception e) {
            log.error("Erro ao atualizar saldo: {}", e.getMessage());
            cacheService.evict(contaId);
            throw e;
        } finally {
            lockService.unlock(lockKey);
        }
    }

    private void processarDebito(Saldo saldo, BigDecimal valor) {
        if (saldo.getSaldoDebito().compareTo(valor) < 0) {
            throw new RuntimeException(
                    String.format("Saldo insuficiente. Disponível: %s", saldo.getSaldoDebito())
            );
        }
        saldo.setSaldoDebito(saldo.getSaldoDebito().subtract(valor));
    }

    private void processarCredito(Saldo saldo, BigDecimal valor) {
        BigDecimal disponivel = saldo.getLimiteCreditoDisponivel();

        if (disponivel.compareTo(valor) < 0) {
            throw new RuntimeException(
                    String.format("Limite insuficiente. Disponível: %s", disponivel)
            );
        }
        saldo.setCreditoUtilizado(saldo.getCreditoUtilizado().add(valor));
    }
}