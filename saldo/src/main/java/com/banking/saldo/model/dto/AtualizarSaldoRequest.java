package com.banking.saldo.model.dto;

import com.banking.saldo.model.Tipo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarSaldoRequest {

    @NotNull(message = "Valor é obrigatório")
    @Positive(message = "Valor deve ser positivo")
    private BigDecimal valor;

    @NotNull(message = "Tipo é obrigatório")
    private Tipo tipo;
}