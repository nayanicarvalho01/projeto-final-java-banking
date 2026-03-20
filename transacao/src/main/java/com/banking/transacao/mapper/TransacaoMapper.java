package com.banking.transacao.mapper;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoDTO;
import com.banking.transacao.model.dto.TransacaoRequestDTO;

public class TransacaoMapper {

    public static TransacaoDTO toResponse(Transacao transacao){
        TransacaoDTO response = new TransacaoDTO();
        response.setId(),
        response.contaId(),
        response.cartaoId(),
        response.valor(),
        response.comerciante(),
        response.localizacao(),
        response.tipoTransacao(),
        response.dataHora();
    }

    public static Transacao toTransacao (TransacaoRequestDTO request){
        Transacao transacao = new Transacao();
        transacao.setContaId(request.getContaId());
        transacao.setValor(request.getValor());
        return transacao;
    }
}
