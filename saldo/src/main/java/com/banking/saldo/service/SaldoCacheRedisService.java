package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Getter
@Setter

@Service
public class SaldoCacheRedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PREFIX = "saldo:";

    public SaldoCacheRedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Saldo get(String contaId) {
        return (Saldo) redisTemplate.opsForValue().get(PREFIX + contaId);
    }

    public void save(Saldo saldo) {
        redisTemplate.opsForValue().set(PREFIX + saldo.getContaId(), saldo);
    }

    public void evict(String contaId) {
        redisTemplate.delete(PREFIX + contaId);
    }
}