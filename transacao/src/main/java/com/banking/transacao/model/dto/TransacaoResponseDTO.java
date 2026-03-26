package com.banking.transacao.model.dto;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoResponseDTO{


    private String id;
    private String contaId;
    private String cartaoId;
    private BigDecimal valor;
    private String comerciante;
    private String localizacao;
    private Tipo tipoTransacao;
    private Status status;
    private Instant dataHora;
}