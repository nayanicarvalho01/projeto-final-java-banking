package com.banking.transacao.model.dto;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
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
public class TransacaoResponse {

    private String id;
    private String contaId;
    private String comerciante;
    private String localizacao;
    private BigDecimal valor;
    private Tipo tipo;
    private Status status;
    private Instant dataHora;

    public static TransacaoResponse fromTransacao(Transacao transacao) {
        if (transacao == null) {
            return null;
        }

        return TransacaoResponse.builder()
                .id(transacao.getId())
                .contaId(transacao.getContaId())
                .comerciante(transacao.getComerciante())
                .localizacao(transacao.getLocalizacao())
                .valor(transacao.getValor())
                .tipo(transacao.getTipo())
                .status(transacao.getStatus())
                .dataHora(transacao.getDataHora())
                .build();
    }
}