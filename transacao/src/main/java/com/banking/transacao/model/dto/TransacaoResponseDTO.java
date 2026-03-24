package com.banking.transacao.model.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TransacaoResponseDTO{

    private String contaId;
    private BigDecimal valor;
}