package com.banking.transacao.worker;

import com.banking.transacao.model.dto.TransacaoEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificarWorker {

    private final KafkaTemplate<String, TransacaoEvent> kafkaTemplate;
    private final ExternalTaskClient externalTaskClient;

    @Value("${camunda.worker.retry.max:3}")
    private int maxRetries;

    @Value("${camunda.worker.retry.delay:1000}")
    private long retryDelay;

    @Value("${camunda.worker.lock-duration:10000}")
    private long lockDuration;

    @PostConstruct
    public void subscribe() {
        externalTaskClient.subscribe("notificar-solicitante")
                .lockDuration(lockDuration)
                .handler(this::handleTask)
                .open();

        log.info("Worker 'notificar-solicitante' iniciado");
    }

    private void handleTask(ExternalTask task, ExternalTaskService service) {
        String contaId = null;

        try {
            log.info("Processando notificação - Task: {}", task.getId());

            TransacaoEvent event = TransacaoEvent.builder()
                    .transacaoId((String) task.getVariable("transacaoId"))
                    .contaId((String) task.getVariable("contaId"))
                    .comerciante((String) task.getVariable("comerciante"))
                    .localizacao((String) task.getVariable("localizacao"))
                    .valor(new BigDecimal(task.getVariable("valor").toString()))
                    .tipo((String) task.getVariable("tipo"))
                    .status((String) task.getVariable("status"))
                    .dataHora(Instant.now())
                    .build();

            contaId = event.getContaId();

            String topic = "APROVADA".equals(event.getStatus())
                    ? "transacoes-aprovadas"
                    : "transacoes-reprovadas";

            kafkaTemplate.send(topic, event.getContaId(), event);

            log.info("Evento publicado no Kafka - Topic: {}, Conta: {}", topic, contaId);

            service.complete(task);

        } catch (Exception e) {
            log.error("Erro ao publicar notificação - Conta: {}", contaId, e);
            service.handleFailure(task, "Erro ao publicar no Kafka", e.getMessage(), maxRetries, retryDelay);
        }
    }
}