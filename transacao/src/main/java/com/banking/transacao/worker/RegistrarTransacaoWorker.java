package com.banking.transacao.worker;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.enumerated.Tipo;
import com.banking.transacao.service.TransacaoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
//@Component
@RequiredArgsConstructor
public class RegistrarTransacaoWorker {

    private final TransacaoService transacaoService;
    private final ExternalTaskClient externalTaskClient;

    @PostConstruct
    public void subscribe() {
        externalTaskClient.subscribe("registrar-transacao")
                .lockDuration(10000)
                .handler((externalTask, externalTaskService) -> {
                    try {
                        log.info("Processando task 'registrar-transacao' - TaskId: {}", externalTask.getId());

                        Map<String, Object> variables = externalTask.getAllVariables();

                        TransacaoRequest request = TransacaoRequest.builder()
                                .contaId((String) variables.get("contaId"))
                                .comerciante((String) variables.get("comerciante"))
                                .localizacao((String) variables.get("localizacao"))
                                .valor(new BigDecimal(variables.get("valor").toString()))
                                .tipo(Tipo.valueOf(variables.get("tipo").toString()))
                                .build();

                        log.info("Registrando transação - ContaId: {}, Valor: {}, Tipo: {}",
                                request.getContaId(), request.getValor(), request.getTipo());

                        Transacao transacao = transacaoService.processar(request);

                        Map<String, Object> outputVariables = new HashMap<>();
                        outputVariables.put("transacaoId", transacao.getId());
                        outputVariables.put("status", transacao.getStatus().name());
                        outputVariables.put("tipo", transacao.getTipo().name());

                        externalTaskService.complete(externalTask, outputVariables);

                        log.info("Task completada - ID: {}, Status: {}",
                                transacao.getId(), transacao.getStatus());

                    } catch (Exception e) {
                        log.error("Erro crítico ao processar task", e);
                        externalTaskService.handleFailure(externalTask,
                                "Erro ao registrar transação", e.getMessage(), 3, 10000);
                    }
                })
                .open();

        log.info("Worker 'registrar-transacao' iniciado");
    }
}