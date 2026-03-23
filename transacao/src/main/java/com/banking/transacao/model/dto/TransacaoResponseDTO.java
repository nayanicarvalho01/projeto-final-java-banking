package com.banking.transacao.model.dto;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TransacaoResponseDTO{
        String id;
        String contaId;
        String cartaoId;
        BigDecimal valor;
        String comerciante;
        String localizacao;
        Tipo tipoTransacao;
        Status status;
        Instant dataHora;


}