package com.banking.transacao.model.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TransacaoRequestDTO {

    private String contaId;
    private BigDecimal valor;
}
