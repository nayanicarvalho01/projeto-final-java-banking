package com.banking.transacao.model.enumerated;

public enum Tipo {
    DEBITO,
    CREDITO;

    public String toTipoSaldo() {
        return this.name();
    }
}