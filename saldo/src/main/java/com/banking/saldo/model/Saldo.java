package com.banking.saldo.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "saldos")
public class Saldo {

    @Id
    @NotNull
    private String contaId;

    @NotNull
    private BigDecimal saldoDebito;

    @NotNull
    private BigDecimal limiteCredito;

    @NotNull
    private BigDecimal creditoUtilizado;

    private Instant ultimaAtualizacao;


    public BigDecimal getLimiteCreditoDisponivel() {
        if (limiteCredito == null || creditoUtilizado == null) {
            return BigDecimal.ZERO;
        }
        return limiteCredito.subtract(creditoUtilizado);
    }
}