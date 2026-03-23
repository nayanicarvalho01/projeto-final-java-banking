package com.banking.transacao.model;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@ToString
public class Transacao {

    String id;
    String contaId;
    String cartaoId;
    BigDecimal valor;
    String comerciante;
    String localizacao;
    Tipo tipoTransacao;
    Status status;
    OffsetDateTime dataHora;
}
