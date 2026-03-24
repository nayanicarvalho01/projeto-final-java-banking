package com.banking.transacao.service;

import com.banking.transacao.mapper.TransacaoMapper;
import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequestDTO;
import com.banking.transacao.model.dto.TransacaoResponseDTO;
import com.banking.transacao.repository.TransacaoRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;


@Service
public class TransacaoService {

    private final TransacaoRepository repository;

    public TransacaoService(TransacaoRepository repository) {
        this.repository = repository;
    }

    public TransacaoResponseDTO novaTransacao(TransacaoRequestDTO requestDTO){
        Transacao transacao = TransacaoMapper.toTransacao(requestDTO);
        transacao.setDataHora(OffsetDateTime.now());

        Transacao salva = repository.save(transacao);
        return TransacaoMapper.toResponse(salva);
    }
}
