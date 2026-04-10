package com.banking.notificacao.kafka;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
import com.banking.notificacao.model.NotificacaoEvent;
import com.banking.notificacao.service.NotificacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacaoConsumer {

    private final NotificacaoService notificacaoService;
    private final ObjectMapper objectMapper;

    @Bean
    public Consumer<String> notificacoes() {
        return mensagem -> {
            try {
                log.info("📩 Mensagem recebida do Kafka");

                // Deserializar JSON
                @SuppressWarnings("unchecked")
                Map<String, Object> map = objectMapper.readValue(mensagem, Map.class);

                // Construir NotificacaoEvent
                NotificacaoEvent event = NotificacaoEvent.builder()
                        .transacaoId((String) map.get("transacaoId"))
                        .contaId((String) map.get("contaId"))
                        .comerciante((String) map.get("comerciante"))
                        .localizacao((String) map.get("localizacao"))
                        .valor(new BigDecimal(map.get("valor").toString()))
                        .tipo(Tipo.valueOf((String) map.get("tipo")))
                        .status(Status.valueOf((String) map.get("status")))
                        .dataHora(Instant.ofEpochSecond(((Number) map.get("dataHora")).longValue()))
                        .build();

                log.info("📩 Evento recebido - Conta: {}, Status: {}, Tipo: {}",
                        event.getContaId(), event.getStatus(), event.getTipo());

                notificacaoService.processar(event);

                log.info("✅ Notificação processada - Conta: {}", event.getContaId());

            } catch (Exception e) {
                log.error("❌ Erro ao processar mensagem: {}", mensagem, e);
            }
        };
    }
}