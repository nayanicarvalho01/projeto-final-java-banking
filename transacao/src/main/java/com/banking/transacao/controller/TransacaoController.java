package com.banking.transacao.controller;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.dto.TransacaoResponse;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping
    public ResponseEntity<TransacaoResponse> processar(
            @RequestBody @Valid TransacaoRequest request) {

        log.info("Recebendo requisição - ContaId: {}", request.getContaId());

        Transacao transacao = transacaoService.processar(request);

        TransacaoResponse response = TransacaoResponse.fromTransacao(transacao);

        HttpStatus status = transacao.getStatus() == Status.APROVADA
                ? HttpStatus.CREATED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/conta/{contaId}")
    public ResponseEntity<List<TransacaoResponse>> listarPorConta(
            @PathVariable String contaId) {

        log.info("Listando transações - ContaId: {}", contaId);

        List<Transacao> transacoes = transacaoService.buscarPorConta(contaId);

        List<TransacaoResponse> response = transacoes.stream()
                .map(TransacaoResponse::fromTransacao)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}