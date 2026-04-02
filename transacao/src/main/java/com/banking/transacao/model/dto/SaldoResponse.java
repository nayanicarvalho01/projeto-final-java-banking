package com.banking.transacao.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoResponse {
    private String contaId;
    private BigDecimal saldoDebito;
    private BigDecimal limiteCredito;
    private BigDecimal creditoUtilizado;
}