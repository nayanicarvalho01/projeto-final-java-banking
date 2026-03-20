package com.banking.transacao.service;

import com.banking.transacao.model.dto.TransacaoDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class TransacaoService {

    private final StringRedisTemplate redisTemplate;

    public TransacaoService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void processarTransacao(TransacaoDTO dto){

    }
}
