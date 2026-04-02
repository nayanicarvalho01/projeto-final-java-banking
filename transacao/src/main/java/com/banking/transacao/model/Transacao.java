package com.banking.transacao.model;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transacoes")
public class Transacao {

    @Id
    private String id;

    private String contaId;

    private String comerciante;

    private String localizacao;

    private BigDecimal valor;

    private Tipo tipo;

    private Status status;

    private Instant dataHora;

}