package com.banking.transacao.mapper;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequestDTO;
import com.banking.transacao.model.dto.TransacaoResponseDTO;

public class TransacaoMapper {

    public static TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setId(dto.getId());
        dto.setCartaoId(dto.getCartaoId());
        dto.setContaId(dto.getContaId());
        dto.setValor(dto.getValor());
        dto.setComerciante(dto.getComerciante());
        dto.setLocalizacao(dto.getLocalizacao());
        dto.setTipoTransacao(dto.getTipoTransacao());
        dto.setStatus(dto.getStatus());
        dto.setDataHora(dto.getDataHora());

        return dto;
    }

    public static Transacao toTransacao(TransacaoRequestDTO requestDTO){
        Transacao transacao = new Transacao();
        transacao.setContaId(requestDTO.getContaId());
        transacao.setValor(requestDTO.getValor());
        return transacao;
    }
}
