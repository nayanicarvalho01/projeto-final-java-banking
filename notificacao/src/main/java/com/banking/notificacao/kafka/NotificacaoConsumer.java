package com.banking.notificacao.kafka;

import com.banking.notificacao.model.NotificacaoEvent;
import com.banking.notificacao.service.NotificacaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoConsumer {

    private final NotificacaoService notificacaoService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {"transacoes-aprovadas", "transacoes-reprovadas"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumir(String mensagem, Acknowledgment ack) {
        try {
            NotificacaoEvent event = objectMapper.readValue(mensagem, NotificacaoEvent.class);

            log.info("Evento recebido - Conta: {}, Status: {}",
                    event.getContaId(), event.getStatus());

            boolean enviado = notificacaoService.processar(event);

            if (enviado) {
                ack.acknowledge();
                log.info("Offset commitado - Conta: {}", event.getContaId());
            } else {
                log.warn("Offset NÃO commitado - Conta: {} (será reprocessado)",
                        event.getContaId());
            }

        } catch (JsonProcessingException e) {
            log.error("JSON inválido (commitando): {}", mensagem, e);
            ack.acknowledge();

        } catch (Exception e) {
            log.error("Erro ao processar evento (será reprocessado)", e);
        }
    }
}