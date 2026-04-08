package com.banking.saldo.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SaldoRedisRepository {

    private final RedisTemplate<String, Long> redisTemplate;

    public void inicializar(String contaId, Long saldoDebitoCentavos, Long limiteCreditoCentavos) {
        redisTemplate.opsForValue().set("conta:" + contaId + ":debito", saldoDebitoCentavos);
        redisTemplate.opsForValue().set("conta:" + contaId + ":credito", limiteCreditoCentavos);

        log.info("Conta inicializada no Redis: {}", contaId);
    }

    public Long debitar(String contaId, Long centavos) {
        Long novoSaldo = redisTemplate.opsForValue()
                .decrement("conta:" + contaId + ":debito", centavos);

        if (novoSaldo == null) {
            throw new RuntimeException("Conta não encontrada: " + contaId);
        }

        if (novoSaldo < 0) {
            redisTemplate.opsForValue().increment("conta:" + contaId + ":debito", centavos);
            throw new RuntimeException("Saldo insuficiente");
        }

        return novoSaldo;
    }

    public Long creditar(String contaId, Long centavos) {
        Long novoDisponivel = redisTemplate.opsForValue()
                .decrement("conta:" + contaId + ":credito", centavos);

        if (novoDisponivel == null) {
            throw new RuntimeException("Conta não encontrada: " + contaId);
        }

        if (novoDisponivel < 0) {
            redisTemplate.opsForValue().increment("conta:" + contaId + ":credito", centavos);
            throw new RuntimeException("Limite insuficiente");
        }

        return novoDisponivel;
    }

    public Long depositar(String contaId, Long centavos) {
        Long novoSaldo = redisTemplate.opsForValue()
                .increment("conta:" + contaId + ":debito", centavos);

        if (novoSaldo == null) {
            throw new RuntimeException("Conta não encontrada: " + contaId);
        }

        return novoSaldo;
    }

    public Long getSaldoDebito(String contaId) {
        Long saldo = redisTemplate.opsForValue().get("conta:" + contaId + ":debito");

        if (saldo == null) {
            throw new RuntimeException("Conta não encontrada: " + contaId);
        }

        return saldo;
    }

    public Long getCreditoDisponivel(String contaId) {
        Long credito = redisTemplate.opsForValue().get("conta:" + contaId + ":credito");

        if (credito == null) {
            throw new RuntimeException("Conta não encontrada: " + contaId);
        }

        return credito;
    }
}