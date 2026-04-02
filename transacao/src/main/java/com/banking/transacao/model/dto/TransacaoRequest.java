package com.banking.transacao.model.dto;

import com.banking.transacao.model.enumerated.Tipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoRequest {

    @NotBlank(message = "ContaId é obrigatório")
    private String contaId;

    @NotBlank(message = "Comerciante é obrigatório")
    private String comerciante;

    @NotBlank(message = "Localização é obrigatória")
    private String localizacao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;

    @NotNull(message = "Tipo de transação é obrigatório")
    private Tipo tipo;
}