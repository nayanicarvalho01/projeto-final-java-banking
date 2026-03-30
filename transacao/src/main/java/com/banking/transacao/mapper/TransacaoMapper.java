package com.banking.transacao.mapper;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequestDTO;
import com.banking.transacao.model.dto.TransacaoResponseDTO;

public class TransacaoMapper {

    public static TransacaoResponseDTO toResponse(Transacao transacao) {
        TransacaoResponseDTO dto = new TransacaoResponseDTO();
        dto.setId(transacao.getId());
        dto.setCartaoId(transacao.getCartaoId());
        dto.setContaId(transacao.getContaId());
        dto.setValor(transacao.getValor());
        dto.setComerciante(transacao.getComerciante());
        dto.setLocalizacao(transacao.getLocalizacao());
        dto.setTipoTransacao(transacao.getTipo());
        dto.setStatus(transacao.getStatus());
        dto.setDataHora(transacao.getDataHora());

        return dto;
    }

    public static Transacao toTransacao(TransacaoRequestDTO requestDTO){
        Transacao transacao = new Transacao();
        transacao.setContaId(requestDTO.getContaId());
        transacao.setCartaoId(requestDTO.getCartaoId());
        transacao.setValor(requestDTO.getValor());
        transacao.setComerciante(requestDTO.getComerciante());
        transacao.setLocalizacao(requestDTO.getLocalizacao());
        transacao.setTipo(requestDTO.getTipoTransacao());
        return transacao;
    }
}
