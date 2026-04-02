package com.banking.saldo.controller;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.dto.CriarContaRequest;
import com.banking.saldo.model.dto.SaldoResponse;
import com.banking.saldo.service.SaldoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/api/saldos")
@RequiredArgsConstructor
@Validated
public class SaldoController {

    private final SaldoService saldoService;

    @PostMapping
    public ResponseEntity<SaldoResponse> criarConta(@RequestBody @Valid CriarContaRequest request) {
        log.info("Criando conta: {}", request.getContaId());

        Saldo saldo = saldoService.criarConta(
                request.getContaId(),
                request.getSaldoInicial(),
                request.getLimiteCredito()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SaldoResponse.fromSaldo(saldo));
    }

    @GetMapping("/{contaId}")
    public ResponseEntity<SaldoResponse> buscarSaldo(@PathVariable String contaId) {
        log.info("Buscando saldo: {}", contaId);

        Saldo saldo = saldoService.buscarSaldo(contaId);

        return ResponseEntity.ok(SaldoResponse.fromSaldo(saldo));
    }

    @PostMapping("/{contaId}/deposito")
    public ResponseEntity<SaldoResponse> depositar(
            @PathVariable String contaId,
            @RequestParam
            @NotNull(message = "Valor é obrigatório")
            @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
            BigDecimal valor) {

        log.info("Depósito - Conta: {}, Valor: {}", contaId, valor);

        Saldo saldo = saldoService.depositar(contaId, valor);

        return ResponseEntity.ok(SaldoResponse.fromSaldo(saldo));
    }

//    @PutMapping("/{contaId}")
//    public ResponseEntity<SaldoResponseDTO> atualizarSaldo(
//            @PathVariable String contaId,
//            @RequestBody @Valid AtualizarSaldoDTO request) {
//
//        log.info("Atualização - Conta: {}, Tipo: {}, Valor: {}",
//                contaId, request.getTipo(), request.getValor());
//
//        Saldo saldo = saldoService.atualizar(contaId, request.getValor(), request.getTipo());
//
//        return ResponseEntity.ok(SaldoResponseDTO.fromSaldo(saldo));
//    }
}