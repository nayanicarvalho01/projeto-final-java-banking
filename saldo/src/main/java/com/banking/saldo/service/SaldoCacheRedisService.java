package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaldoCacheRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "saldo:";
    private static final long TTL_MINUTES = 60;

    public Saldo get(String contaId) {
        try {
            Object obj = redisTemplate.opsForValue().get(PREFIX + contaId);

            if (obj instanceof Saldo saldo) {
                log.debug("Cache HIT: {}", contaId);
                return saldo;
            }

            log.debug("Cache MISS: {}", contaId);
            return null;

        } catch (Exception e) {
            log.error("Erro no cache (get): {}", e.getMessage());
            return null;
        }
    }

    public void save(Saldo saldo) {
        if (saldo == null || saldo.getContaId() == null) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(
                    PREFIX + saldo.getContaId(),
                    saldo,
                    Duration.ofMinutes(TTL_MINUTES)
            );
            log.debug("Cache salvo: {}", saldo.getContaId());

        } catch (Exception e) {
            log.error("Erro no cache (save): {}", e.getMessage());
        }
    }

    public void evict(String contaId) {
        try {
            redisTemplate.delete(PREFIX + contaId);
            log.debug("Cache invalidado: {}", contaId);

        } catch (Exception e) {
            log.error("Erro no cache (evict): {}", e.getMessage());
        }
    }
}