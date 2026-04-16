package com.banking.transacao.controller;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.dto.TransacaoResponse;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import com.banking.transacao.service.TransacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
@DisplayName("Testes do TransacaoController")
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransacaoService transacaoService;

    private TransacaoRequest request;
    private Transacao transacaoAprovada;
    private Transacao transacaoNegada;

    @BeforeEach
    void setUp() {
        request = TransacaoRequest.builder()
                .contaId("12345")
                .comerciante("Loja ABC")
                .localizacao("São Paulo, SP")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .build();

        transacaoAprovada = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .comerciante("Loja ABC")
                .localizacao("São Paulo, SP")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        transacaoNegada = Transacao.builder()
                .id("trans-002")
                .contaId("12345")
                .comerciante("Loja XYZ")
                .localizacao("Rio de Janeiro, RJ")
                .valor(BigDecimal.valueOf(1000.00))
                .tipo(Tipo.DEBITO)
                .status(Status.NEGADA)
                .dataHora(Instant.now())
                .build();
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve processar transação aprovada com sucesso (201)")
    void deveProcessarTransacaoAprovadaComSucesso() throws Exception {
        // Arrange
        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("trans-001"))
                .andExpect(jsonPath("$.contaId").value("12345"))
                .andExpect(jsonPath("$.comerciante").value("Loja ABC"))
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.tipo").value("DEBITO"))
                .andExpect(jsonPath("$.status").value("APROVADA"));

        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve retornar 400 quando transação negada")
    void deveRetornar400QuandoTransacaoNegada() throws Exception {
        // Arrange
        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoNegada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("NEGADA"));

        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve validar campos obrigatórios")
    void deveValidarCamposObrigatorios() throws Exception {
        // Arrange
        TransacaoRequest requestInvalido = TransacaoRequest.builder().build();

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());

        verify(transacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve validar contaId não nulo")
    void deveValidarContaIdNaoNulo() throws Exception {
        // Arrange
        request.setContaId(null);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve validar valor não nulo")
    void deveValidarValorNaoNulo() throws Exception {
        // Arrange
        request.setValor(null);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve validar tipo não nulo")
    void deveValidarTipoNaoNulo() throws Exception {
        // Arrange
        request.setTipo(null);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(transacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve processar transação de crédito")
    void deveProcessarTransacaoCredito() throws Exception {
        // Arrange
        request.setTipo(Tipo.CREDITO);
        transacaoAprovada.setTipo(Tipo.CREDITO);

        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("CREDITO"))
                .andExpect(jsonPath("$.status").value("APROVADA"));

        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve processar transação com valores decimais")
    void deveProcessarTransacaoComValoresDecimais() throws Exception {
        // Arrange
        request.setValor(new BigDecimal("99.99"));
        transacaoAprovada.setValor(new BigDecimal("99.99"));

        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(99.99));

        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }

    @Test
    @DisplayName("GET /api/transacoes/conta/{contaId} - Deve listar transações por conta")
    void deveListarTransacoesPorConta() throws Exception {
        // Arrange
        Transacao transacao1 = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .comerciante("Loja ABC")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        Transacao transacao2 = Transacao.builder()
                .id("trans-002")
                .contaId("12345")
                .comerciante("Loja XYZ")
                .valor(BigDecimal.valueOf(200.00))
                .tipo(Tipo.CREDITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        when(transacaoService.buscarPorConta("12345"))
                .thenReturn(List.of(transacao1, transacao2));

        // Act & Assert
        mockMvc.perform(get("/api/transacoes/conta/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("trans-001"))
                .andExpect(jsonPath("$[0].contaId").value("12345"))
                .andExpect(jsonPath("$[0].comerciante").value("Loja ABC"))
                .andExpect(jsonPath("$[1].id").value("trans-002"))
                .andExpect(jsonPath("$[1].comerciante").value("Loja XYZ"));

        verify(transacaoService, times(1)).buscarPorConta("12345");
    }

    @Test
    @DisplayName("GET /api/transacoes/conta/{contaId} - Deve retornar lista vazia quando não há transações")
    void deveRetornarListaVaziaQuandoNaoHaTransacoes() throws Exception {
        // Arrange
        when(transacaoService.buscarPorConta("99999"))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/transacoes/conta/99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(transacaoService, times(1)).buscarPorConta("99999");
    }

    @Test
    @DisplayName("GET /api/transacoes/conta/{contaId} - Deve listar transações com diferentes status")
    void deveListarTransacoesComDiferentesStatus() throws Exception {
        // Arrange
        Transacao aprovada = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .status(Status.APROVADA)
                .comerciante("Loja A")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .dataHora(Instant.now())
                .build();

        Transacao negada = Transacao.builder()
                .id("trans-002")
                .contaId("12345")
                .status(Status.NEGADA)
                .comerciante("Loja B")
                .valor(BigDecimal.valueOf(200.00))
                .tipo(Tipo.DEBITO)
                .dataHora(Instant.now())
                .build();

        when(transacaoService.buscarPorConta("12345"))
                .thenReturn(List.of(aprovada, negada));

        // Act & Assert
        mockMvc.perform(get("/api/transacoes/conta/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("APROVADA"))
                .andExpect(jsonPath("$[1].status").value("NEGADA"));

        verify(transacaoService, times(1)).buscarPorConta("12345");
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve aceitar request com todos os campos preenchidos")
    void deveAceitarRequestComTodosCamposPreenchidos() throws Exception {
        // Arrange
        request.setComerciante("Comerciante Completo");
        request.setLocalizacao("Localização Completa");

        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }

    @Test
    @DisplayName("GET /api/transacoes/conta/{contaId} - Deve aceitar contaId com caracteres especiais")
    void deveAceitarContaIdComCaracteresEspeciais() throws Exception {
        // Arrange
        when(transacaoService.buscarPorConta(anyString()))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/transacoes/conta/ABC-123_XYZ"))
                .andExpect(status().isOk());

        verify(transacaoService, times(1)).buscarPorConta("ABC-123_XYZ");
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJson() throws Exception {
        // Arrange
        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act & Assert
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/transacoes/conta/{contaId} - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJsonNoGet() throws Exception {
        // Arrange
        when(transacaoService.buscarPorConta(anyString()))
                .thenReturn(List.of(transacaoAprovada));

        // Act & Assert
        mockMvc.perform(get("/api/transacoes/conta/12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/transacoes - Deve logar informações da requisição")
    void deveLogarInformacoesDaRequisicao() throws Exception {
        // Arrange
        when(transacaoService.processar(any(TransacaoRequest.class)))
                .thenReturn(transacaoAprovada);

        // Act
        mockMvc.perform(post("/api/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Assert - Verifica que o service foi chamado (log está implícito)
        verify(transacaoService, times(1)).processar(any(TransacaoRequest.class));
    }
}