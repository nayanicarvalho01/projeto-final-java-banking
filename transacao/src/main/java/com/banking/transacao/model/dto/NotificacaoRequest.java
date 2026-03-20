package com.banking.transacao.model.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;

public class NotificacaoRequest {

    @NotBlank(message = "O ID não pode ser vazio, nulo ou somente espaços")
    String id;

    @NotBlank(message = "O valor não pode ser vazio, nulo ou somente espaços")
    String valor;

    @NotBlank(message = "A data de processamento não pode ser vazio, nulo ou somente espaços")
    LocalDateTime dataHora;
}
