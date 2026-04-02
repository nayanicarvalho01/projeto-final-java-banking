package com.banking.saldo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long TIMEOUT_MS = 5000L;

    public boolean lock(String key) {
        String token = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, token, Duration.ofMillis(TIMEOUT_MS));

        if (Boolean.TRUE.equals(success)) {
            log.debug("Lock adquirido: {}", key);
            return true;
        }

        log.debug("Falha ao adquirir lock: {}", key);
        return false;
    }

    public void unlock(String key) {
        redisTemplate.delete(key);
        log.debug("Lock liberado: {}", key);
    }
}