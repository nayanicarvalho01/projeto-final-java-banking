package com.banking.saldo.model;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Saldo {

    @Id
    String contaId;

    private BigDecimal saldoDebito;

    private BigDecimal saldoCredito;

    private Instant ultimaAtualizacao;
}
