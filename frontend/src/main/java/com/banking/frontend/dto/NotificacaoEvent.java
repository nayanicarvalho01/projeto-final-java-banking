package com.banking.frontend.dto;

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
public class NotificacaoEvent {

    private String transacaoId;
    private String contaId;
    private String comerciante;
    private String localizacao;
    private BigDecimal valor;
    private Tipo tipo;
    private Status status;
    private Instant dataHora;

    public enum Tipo {
        DEBITO, CREDITO
    }

    public enum Status {
        APROVADA, NEGADA
    }
}