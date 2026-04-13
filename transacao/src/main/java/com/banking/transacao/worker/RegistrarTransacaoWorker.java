package com.banking.transacao.worker;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.repository.TransacaoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegistrarTransacaoWorker {

    private final TransacaoRepository transacaoRepository;
    private final ExternalTaskClient externalTaskClient;

    @PostConstruct
    public void subscribe() {
        externalTaskClient.subscribe("registrar-transacao")
                .handler(this::handleTask)
                .open();

        log.info("Worker 'registrar-transacao' iniciado");
    }

    private void handleTask(ExternalTask task, ExternalTaskService service) {
        String transacaoId = null;

        try {
            transacaoId = (String) task.getVariable("transacaoId");

            log.info("Processando task - Transação: {}", transacaoId);

            Transacao transacao = transacaoRepository.findById(transacaoId)
                    .orElseThrow(() -> new RuntimeException("Transação não encontrada"));

            log.info("Transação confirmada - ID: {}", transacao.getId());

            service.complete(task);

        } catch (Exception e) {
            log.error("Erro ao processar task - Transação: {}", transacaoId, e);

            service.handleFailure(task, "Erro ao registrar transação", e.getMessage(), 3, 10000);
        }
    }
}