package com.banking.saldo.service;

import com.banking.saldo.model.dto.SaldoResponseDTO;
import com.banking.saldo.repository.SaldoRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter

@Service
public class SaldoService {

    private final SaldoRepository saldoRepository;

    public SaldoService(SaldoRepository saldoRepository) {
        this.saldoRepository = saldoRepository;
    }

    public SaldoResponseDTO atualizarSaldo() {
        return null;
    }
}
