package com.banking.transacao.model;

import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;

import java.time.LocalDateTime;

public class Notificacao {

    String id;
    String valor;
    LocalDateTime dataHora;
    Status status;
    Tipo tipo;
}
