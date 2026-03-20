package com.banking.transacao.service;

import com.banking.transacao.mapper.TransacaoMapper;
import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoDTO;
import com.banking.transacao.model.dto.TransacaoRequestDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
public class TransacaoService {

    private final StringRedisTemplate redisTemplate;

    public TransacaoService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
