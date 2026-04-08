package com.banking.notificacao.model;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
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
}