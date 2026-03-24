package com.banking.transacao.worker;

import com.banking.transacao.model.dto.TransacaoRequestDTO;
import com.banking.transacao.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.ExternalTaskClient;

import java.util.Map;

public class RegistrarTransacao {

    private ObjectMapper objectMapper;

    public RegistrarTransacao(ExternalTaskClient client, TransacaoService service){

        client.subscribe("registrar-transacao")
                .lockDuration(10000)
                .handler((task, serviceTask)-> {

                    Map<String, Object> variaveis = task.getAllVariables();
                    TransacaoRequestDTO dto = objectMapper.convertValue(variaveis, TransacaoRequestDTO.class);

                    service.novaTransacao(dto);
                });
    }
}
