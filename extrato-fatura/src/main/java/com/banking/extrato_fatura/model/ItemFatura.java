package com.banking.extrato_fatura.model;

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
public class ItemFatura {

    private String transacaoId;
    private String comerciante;
    private String localizacao;
    private BigDecimal valor;
    private Instant dataHora;
}