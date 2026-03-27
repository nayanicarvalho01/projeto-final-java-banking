package com.banking.saldo.worker;

import com.banking.saldo.model.Tipo;
import com.banking.saldo.model.dto.AtualizarSaldoDTO;
import com.banking.saldo.service.SaldoService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AtualizarSaldoWorker {

    public AtualizarSaldoWorker(SaldoService saldoService, ObjectMapper objectMapper,
                                ExternalTaskClient externalTaskClient) {

        externalTaskClient.subscribe("atualizar-saldo")
                .lockDuration(20000)
                .handler((task, serviceTask) -> {

                    String businessKey = task.getBusinessKey();

                    try {
                        String contaId = (String) task.getVariable("contaId");
                        BigDecimal valor = new BigDecimal(task.getVariable("valor").toString());
                        String tipoStr = (String) task.getVariable("tipo");
                        Tipo tipo = Tipo.valueOf(tipoStr);

                        AtualizarSaldoDTO dto = AtualizarSaldoDTO
                                .valor(valor)
                                .tipo(tipo)
                                .build();
                        saldoService.atualizarSaldo(dto);

                        Map<String, Object> retorno = new HashMap<>();
                        retorno.put("saldoAtualizado", true);

                        serviceTask.complete(task, retorno);
                    }catch (Exception e){
                        serviceTask.handleFailure(task, "Erro ao atualizar saldo", e.getMessage(), 3, 1000);
                    }
                });
    }
}
