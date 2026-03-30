package com.banking.saldo.worker;

import com.banking.saldo.model.Tipo;
import com.banking.saldo.service.SaldoService;
import org.camunda.bpm.client.ExternalTaskClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AtualizarSaldoWorker {

    public AtualizarSaldoWorker(SaldoService saldoService,
                                ExternalTaskClient externalTaskClient) {

        externalTaskClient.subscribe("atualizar-saldo")
                .lockDuration(20000)
                .handler((task, serviceTask) -> {

                    try {

                        String contaId = (String) task.getVariable("contaId");
                        BigDecimal valor = new BigDecimal(task.getVariable("valor").toString());
                        Tipo tipo = Tipo.valueOf((String) task.getVariable("tipo"));

                        // 🔥 chamada correta do service
                        saldoService.atualizar(contaId, valor, tipo);

                        Map<String, Object> retorno = new HashMap<>();
                        retorno.put("saldoAtualizado", true);

                        serviceTask.complete(task, retorno);

                    } catch (Exception e) {

                        serviceTask.handleFailure(
                                task,
                                "Erro ao atualizar saldo",
                                e.getMessage(),
                                3,
                                1000
                        );
                    }

                })
                .open(); // 🔴 ESSENCIAL
    }
}