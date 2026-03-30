package com.banking.transacao.model.dto;

import com.banking.transacao.model.enumerated.Tipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TransacaoRequestDTO {

    @NotBlank(message = "ContaId obrigatório")
    String contaId;

    @NotBlank(message = "CartaoId obrigatório")
    String cartaoId;

    @NotNull(message = "Valor obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal valor;


    String comerciante;

    @NotBlank(message = "Localização obrigatória")
    String localizacao;

    @NotNull(message = "Tipo de transação é obrigatório")
    Tipo tipoTransacao;
}
