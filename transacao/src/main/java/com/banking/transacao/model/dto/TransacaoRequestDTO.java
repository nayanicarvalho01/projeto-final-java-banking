package com.banking.transacao.model.dto;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TransacaoRequestDTO {

    String id;
    String contaId;
    String cartaoId;
    BigDecimal valor;
    String comerciante;
    String localizacao;
    Tipo tipoTransacao;
    Status status;
    Instant dataHora;
}
