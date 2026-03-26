package com.banking.transacao.worker;

import com.banking.transacao.model.dto.TransacaoRequestDTO;
import com.banking.transacao.model.dto.TransacaoResponseDTO;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class RegistrarTransacao {

    public RegistrarTransacao(TransacaoService transacaoService, ExternalTaskClient externalTaskClient,
                              ObjectMapper objectMapper, TransacaoService service) {

        externalTaskClient.subscribe("registrar-transacao")
                .lockDuration(10000)
                .handler((task, serviceTask) -> {

                    try {

                        Map<String, Object> variaveis = task.getAllVariables();
                        TransacaoRequestDTO dto = objectMapper.convertValue(variaveis, TransacaoRequestDTO.class);

                        TransacaoResponseDTO responseDTO = transacaoService.novaTransacao(dto);

                        Map<String, Object> variaveisRetorno = new HashMap<>();
                        variaveisRetorno.put("transacaoId", responseDTO.getId());
                        variaveisRetorno.put("status", Status.APROVADA.name());

                        serviceTask.complete(task, variaveisRetorno);

                    } catch (Exception e) {

                        serviceTask.handleFailure(
                                task,
                                "Erro ao registrar transação: " + e.getClass().getSimpleName(),
                                e.getMessage(),
                                3,
                                10000   // 10 segundos entre tentativas
                        );

                    }
                })
                .open();

        log.info("Worker 'registrar-transacao' iniciado e aguardando tarefas");
    }
}