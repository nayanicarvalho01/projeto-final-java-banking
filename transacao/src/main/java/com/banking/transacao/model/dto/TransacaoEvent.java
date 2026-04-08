package com.banking.transacao.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoEvent {

    private String transacaoId;
    private String contaId;
    private String comerciante;
    private String localizacao;
    private BigDecimal valor;
    private String tipo;
    private String status;
    private Instant dataHora;

    public static TransacaoEvent fromTransacao(com.banking.transacao.model.Transacao transacao) {
        return TransacaoEvent.builder()
                .transacaoId(transacao.getId())
                .contaId(transacao.getContaId())
                .comerciante(transacao.getComerciante())
                .localizacao(transacao.getLocalizacao())
                .valor(transacao.getValor())
                .tipo(transacao.getTipo().name())
                .status(transacao.getStatus().name())
                .dataHora(transacao.getDataHora())
                .build();
    }
}