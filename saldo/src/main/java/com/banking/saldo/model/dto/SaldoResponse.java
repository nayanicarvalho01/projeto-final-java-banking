package com.banking.saldo.model.dto;

import com.banking.saldo.model.Saldo;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaldoResponse {

    private String contaId;
    private BigDecimal saldoDebito;
    private BigDecimal limiteCredito;
    private BigDecimal creditoUtilizado;
    private BigDecimal creditoDisponivel;

    public static SaldoResponse fromSaldo(Saldo saldo) {
        if (saldo == null) {
            return null;
        }

        return SaldoResponse.builder()
                .contaId(saldo.getContaId())
                .saldoDebito(saldo.getSaldoDebito())
                .limiteCredito(saldo.getLimiteCredito())
                .creditoUtilizado(saldo.getCreditoUtilizado())
                .creditoDisponivel(saldo.getLimiteCreditoDisponivel())
                .build();
    }
}