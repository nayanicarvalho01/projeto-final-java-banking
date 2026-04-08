package com.banking.extrato_fatura.worker;

import com.banking.extrato_fatura.service.FaturaService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarFaturaWorker {

    private final FaturaService faturaService;
    private final ExternalTaskClient externalTaskClient;

    @Value("${camunda.worker.retry.max:3}")
    private int maxRetries;

    @Value("${camunda.worker.retry.delay:1000}")
    private long retryDelay;

    @Value("${camunda.worker.lock-duration:20000}")
    private long lockDuration;

    @PostConstruct
    public void subscribe() {
        externalTaskClient.subscribe("atualizar-fatura")
                .lockDuration(lockDuration)
                .handler(this::handleTask)
                .open();

        log.info("Worker 'atualizar-fatura' iniciado");
    }

    private void handleTask(ExternalTask task, ExternalTaskService service) {
        String contaId = null;

        try {
            log.info("Processando fatura - Task: {}", task.getId());

            contaId = (String) task.getVariable("contaId");
            String transacaoId = (String) task.getVariable("transacaoId");
            String comerciante = (String) task.getVariable("comerciante");
            String localizacao = (String) task.getVariable("localizacao");
            BigDecimal valor = new BigDecimal(task.getVariable("valor").toString());
            Instant dataHora = Instant.now();

            faturaService.adicionarItem(contaId, transacaoId, comerciante, localizacao, valor, dataHora);

            log.info("Fatura atualizada - Conta: {}, Transação: {}", contaId, transacaoId);

            service.complete(task);

        } catch (Exception e) {
            log.error("Erro ao atualizar fatura - Conta: {}, Task: {}", contaId, task.getId(), e);
            service.handleFailure(task, "Erro ao atualizar fatura", e.getMessage(), maxRetries, retryDelay);
        }
    }
}