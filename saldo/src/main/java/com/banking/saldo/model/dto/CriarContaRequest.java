package com.banking.saldo.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CriarContaRequest {

    @NotBlank(message = "ContaId é obrigatório")
    private String contaId;

    @NotNull(message = "Saldo inicial é obrigatório")
    @PositiveOrZero(message = "Saldo inicial deve ser maior ou igual a zero")
    private BigDecimal saldoInicial;

    @NotNull(message = "Limite de crédito é obrigatório")
    @PositiveOrZero(message = "Limite de crédito deve ser maior ou igual a zero")
    private BigDecimal limiteCredito;
}