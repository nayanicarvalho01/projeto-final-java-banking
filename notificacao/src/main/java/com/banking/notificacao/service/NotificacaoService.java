package com.banking.notificacao.service;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
import com.banking.notificacao.model.NotificacaoEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NotificacaoService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public boolean processar(NotificacaoEvent event) {
        log.info("Processando notificação - Conta: {}, Status: {}, Tipo: {}",
                event.getContaId(), event.getStatus(), event.getTipo());

        return enviarSSE(event);
    }

    private boolean enviarSSE(NotificacaoEvent event) {
        SseEmitter emitter = emitters.get(event.getContaId());

        if (emitter == null) {
            log.warn("Usuário offline - Conta: {}", event.getContaId());
            return false;
        }

        try {
            String mensagem = formatarMensagem(event);

            emitter.send(SseEmitter.event()
                    .name("notificacao")
                    .data(mensagem));

            log.info("Notificação enviada - Conta: {}", event.getContaId());
            return true;

        } catch (IOException e) {
            log.error("Erro ao enviar SSE - Conta: {}", event.getContaId(), e);
            emitters.remove(event.getContaId());
            return false;
        }
    }

    private String formatarMensagem(NotificacaoEvent event) {
        return event.getStatus() == Status.APROVADA
                ? formatarAprovada(event)
                : formatarNegada(event);
    }

    private String formatarAprovada(NotificacaoEvent event) {
        String tipo = event.getTipo() == Tipo.DEBITO ? "Débito" : "Crédito";
        return String.format(
                "Transação APROVADA (%s): %s - R$ %.2f em %s",
                tipo,
                event.getComerciante(),
                event.getValor(),
                event.getLocalizacao()
        );
    }

    private String formatarNegada(NotificacaoEvent event) {
        String tipo = event.getTipo() == Tipo.DEBITO ? "Débito" : "Crédito";
        String motivo = event.getTipo() == Tipo.DEBITO
                ? "Saldo insuficiente"
                : "Limite insuficiente";

        return String.format(
                "Transação NEGADA (%s): %s - R$ %.2f (%s)",
                tipo,
                event.getComerciante(),
                event.getValor(),
                motivo
        );
    }

    public SseEmitter registrarCliente(String contaId) {
        log.info("Cliente conectado via SSE - Conta: {}", contaId);

        SseEmitter emitter = new SseEmitter(3600000L); // 1 hora

        emitters.put(contaId, emitter);

        emitter.onCompletion(() -> {
            log.info("SSE concluído - Conta: {}", contaId);
            emitters.remove(contaId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE timeout - Conta: {}", contaId);
            emitters.remove(contaId);
        });

        emitter.onError(e -> {
            log.error("SSE erro - Conta: {}", contaId, e);
            emitters.remove(contaId);
        });

        return emitter;
    }
}