package com.banking.saldo.model.dto;

import com.banking.saldo.model.Tipo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class AtualizarSaldoDTO {

    @Id
    private String contaId;

    private BigDecimal valor;

    private Tipo tipo;
}
