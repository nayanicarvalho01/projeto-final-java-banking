package com.banking.notificacao.controller;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
import com.banking.notificacao.model.NotificacaoEvent;
import com.banking.notificacao.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final NotificacaoService notificacaoService;

    @PostMapping("/notificar/{contaId}")
    public ResponseEntity<String> testarNotificacao(
            @PathVariable String contaId,
            @RequestParam(defaultValue = "APROVADA") String status,
            @RequestParam(defaultValue = "DEBITO") String tipo) {

        NotificacaoEvent event = NotificacaoEvent.builder()
                .transacaoId("test-" + System.currentTimeMillis())
                .contaId(contaId)
                .comerciante("Mercado Teste")
                .localizacao("São Paulo")
                .valor(new BigDecimal("50.00"))
                .tipo(Tipo.valueOf(tipo))
                .status(Status.valueOf(status))
                .dataHora(Instant.now())
                .build();

        boolean enviado = notificacaoService.processar(event);

        return ResponseEntity.ok(enviado
                ? "✅ Notificação enviada com sucesso"
                : "⚠️ Usuário offline");
    }
}