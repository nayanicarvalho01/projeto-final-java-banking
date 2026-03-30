package com.banking.transacao.model;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@ToString
public class Transacao {

    @Id
    private String id;

    private String contaId;
    private String cartaoId;
    private String comerciante;
    private String localizacao;
    private BigDecimal valor;
    private Tipo tipo;
    private Status status;
    private Instant dataHora;
}
