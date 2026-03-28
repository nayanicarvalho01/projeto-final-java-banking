package com.banking.saldo.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Document(collection = "saldo")
public class Saldo {

    @Id
    String contaId;

    private BigDecimal saldoDebito;

    private BigDecimal limiteCredito;

    private Instant ultimaAtualizacao;
}
