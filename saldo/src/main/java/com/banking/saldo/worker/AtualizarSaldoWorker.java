package com.banking.saldo.worker;

import com.banking.saldo.model.Tipo;
import com.banking.saldo.service.SaldoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtualizarSaldoWorker {

    private final SaldoService saldoService;
    private final ExternalTaskClient externalTaskClient;

    @Value("${camunda.worker.retry.max:3}")
    private int maxRetries;

    @Value("${camunda.worker.retry.delay:1000}")
    private long retryDelay;

    @Value("${camunda.worker.lock-duration:20000}")
    private long lockDuration;

    @PostConstruct
    public void subscribe() {
        externalTaskClient.subscribe("atualizar-saldo")
                .lockDuration(lockDuration)
                .handler(this::handleTask)
                .open();

        log.info("Worker 'atualizar-saldo' iniciado");
    }

    private void handleTask(ExternalTask task, ExternalTaskService service) {
        String contaId = null;

        try {
            log.info("Processando task: {}", task.getId());

            contaId = extrairString(task, "contaId");
            BigDecimal valor = extrairBigDecimal(task, "valor");
            Tipo tipo = extrairTipo(task, "tipo");

            saldoService.atualizar(contaId, valor, tipo);

            Map<String, Object> resultado = Map.of(
                    "saldoAtualizado", true,
                    "contaId", contaId
            );

            service.complete(task, resultado);
            log.info("Task concluída - Conta: {}", contaId);

        } catch (IllegalArgumentException e) {
            log.error("Validação falhou - Conta: {}, Erro: {}", contaId, e.getMessage());
            service.handleBpmnError(task, "VALIDATION_ERROR", e.getMessage());

        } catch (RuntimeException e) {
            tratarErroNegocio(task, service, contaId, e);

        } catch (Exception e) {
            log.error("Erro inesperado - Conta: {}", contaId, e);
            handleRetry(task, service, "Erro inesperado", e.getMessage());
        }
    }

    private void tratarErroNegocio(ExternalTask task, ExternalTaskService service,
                                   String contaId, RuntimeException e) {
        String msg = e.getMessage();

        if (msg.contains("insuficiente")) {
            log.warn("Saldo/Limite insuficiente - Conta: {}", contaId);
            service.handleBpmnError(task, "INSUFFICIENT_FUNDS", msg);

        } else if (msg.contains("em processamento")) {
            log.warn("Conta bloqueada - Conta: {} (retry)", contaId);
            handleRetry(task, service, "Conta em processamento", msg);

        } else {
            log.error("Erro ao processar - Conta: {}", contaId, e);
            handleRetry(task, service, "Erro ao atualizar saldo", msg);
        }
    }

    private String extrairString(ExternalTask task, String variavel) {
        String valor = (String) task.getVariable(variavel);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(variavel + " inválido");
        }
        return valor;
    }

    private BigDecimal extrairBigDecimal(ExternalTask task, String variavel) {
        Object obj = task.getVariable(variavel);
        if (obj == null) {
            throw new IllegalArgumentException(variavel + " não encontrado");
        }
        try {
            return new BigDecimal(obj.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(variavel + " inválido: " + obj);
        }
    }

    private Tipo extrairTipo(ExternalTask task, String variavel) {
        String valor = (String) task.getVariable(variavel);
        if (valor == null) {
            throw new IllegalArgumentException(variavel + " não encontrado");
        }
        try {
            return Tipo.valueOf(valor.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(variavel + " inválido: " + valor);
        }
    }

    private void handleRetry(ExternalTask task, ExternalTaskService service,
                             String errorMessage, String errorDetails) {
        service.handleFailure(task, errorMessage, errorDetails, maxRetries, retryDelay);
    }
}