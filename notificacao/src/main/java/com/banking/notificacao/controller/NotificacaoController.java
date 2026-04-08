package com.banking.notificacao.controller;


import com.banking.notificacao.service.NotificacaoService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping(value = "/stream/{contaId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable @NotBlank String contaId) {
        log.info("Iniciando stream SSE - Conta: {}", contaId);
        return notificacaoService.registrarCliente(contaId);
    }
}