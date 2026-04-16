package com.banking.saldo.controller;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.Tipo;
import com.banking.saldo.model.dto.AtualizarSaldoRequest;
import com.banking.saldo.model.dto.CriarContaRequest;
import com.banking.saldo.service.SaldoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SaldoController.class)
@DisplayName("Testes do SaldoController")
class SaldoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SaldoService saldoService;

    private Saldo saldo;
    private CriarContaRequest criarContaRequest;
    private AtualizarSaldoRequest atualizarSaldoRequest;

    @BeforeEach
    void setUp() {
        saldo = Saldo.builder()
                .contaId("12345")
                .saldoDebito(BigDecimal.valueOf(1000.00))
                .limiteCredito(BigDecimal.valueOf(500.00))
                .creditoUtilizado(BigDecimal.ZERO)
                .ultimaAtualizacao(Instant.now())
                .build();

        criarContaRequest = CriarContaRequest.builder()
                .contaId("12345")
                .saldoInicial(BigDecimal.valueOf(1000.00))
                .limiteCredito(BigDecimal.valueOf(500.00))
                .build();

        atualizarSaldoRequest = new AtualizarSaldoRequest(
                BigDecimal.valueOf(100.00),
                Tipo.DEBITO
        );
    }

    @Test
    @DisplayName("POST /api/saldos - Deve criar conta com sucesso (201)")
    void deveCriarContaComSucesso() throws Exception {
        // Arrange
        when(saldoService.criarConta(anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contaId").value("12345"))
                .andExpect(jsonPath("$.saldoDebito").value(1000.00))
                .andExpect(jsonPath("$.limiteCredito").value(500.00))
                .andExpect(jsonPath("$.creditoUtilizado").value(0.0));

        verify(saldoService, times(1)).criarConta(
                eq("12345"),
                eq(BigDecimal.valueOf(1000.00)),
                eq(BigDecimal.valueOf(500.00))
        );
    }

    @Test
    @DisplayName("POST /api/saldos - Deve validar contaId obrigatório")
    void deveValidarContaIdObrigatorio() throws Exception {
        // Arrange
        criarContaRequest.setContaId(null);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).criarConta(anyString(), any(), any());
    }

    @Test
    @DisplayName("POST /api/saldos - Deve validar saldoInicial obrigatório")
    void deveValidarSaldoInicialObrigatorio() throws Exception {
        // Arrange
        criarContaRequest.setSaldoInicial(null);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).criarConta(anyString(), any(), any());
    }

    @Test
    @DisplayName("POST /api/saldos - Deve validar limiteCredito obrigatório")
    void deveValidarLimiteCreditoObrigatorio() throws Exception {
        // Arrange
        criarContaRequest.setLimiteCredito(null);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).criarConta(anyString(), any(), any());
    }

    @Test
    @DisplayName("POST /api/saldos - Deve validar saldoInicial não negativo")
    void deveValidarSaldoInicialNaoNegativo() throws Exception {
        // Arrange
        criarContaRequest.setSaldoInicial(BigDecimal.valueOf(-100.00));

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).criarConta(anyString(), any(), any());
    }

    @Test
    @DisplayName("POST /api/saldos - Deve criar conta com saldo inicial zero")
    void deveCriarContaComSaldoInicialZero() throws Exception {
        // Arrange
        criarContaRequest.setSaldoInicial(BigDecimal.ZERO);
        saldo.setSaldoDebito(BigDecimal.ZERO);

        when(saldoService.criarConta(anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoDebito").value(0.0));

        verify(saldoService, times(1)).criarConta(
                eq("12345"),
                eq(BigDecimal.ZERO),
                any(BigDecimal.class)
        );
    }

    @Test
    @DisplayName("GET /api/saldos/{contaId} - Deve buscar saldo com sucesso")
    void deveBuscarSaldoComSucesso() throws Exception {
        // Arrange
        when(saldoService.buscarSaldo("12345")).thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(get("/api/saldos/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value("12345"))
                .andExpect(jsonPath("$.saldoDebito").value(1000.00))
                .andExpect(jsonPath("$.limiteCredito").value(500.00))
                .andExpect(jsonPath("$.creditoUtilizado").value(0.0));

        verify(saldoService, times(1)).buscarSaldo("12345");
    }

    @Test
    @DisplayName("POST /api/saldos/{contaId}/deposito - Deve validar valor obrigatório")
    void deveValidarValorObrigatorioNoDeposito() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/saldos/12345/deposito"))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).depositar(anyString(), any());
    }

    @Test
    @DisplayName("POST /api/saldos/{contaId}/deposito - Deve aceitar valores decimais")
    void deveAceitarValoresDecimaisNoDeposito() throws Exception {
        // Arrange
        when(saldoService.depositar(anyString(), any(BigDecimal.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(post("/api/saldos/12345/deposito")
                        .param("valor", "99.99"))
                .andExpect(status().isOk());

        verify(saldoService, times(1)).depositar("12345", new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("PUT /api/saldos/{contaId} - Deve atualizar saldo com débito")
    void deveAtualizarSaldoComDebito() throws Exception {
        // Arrange
        saldo.setSaldoDebito(BigDecimal.valueOf(900.00));
        when(saldoService.atualizar(anyString(), any(BigDecimal.class), any(Tipo.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(put("/api/saldos/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarSaldoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value("12345"))
                .andExpect(jsonPath("$.saldoDebito").value(900.00));

        verify(saldoService, times(1)).atualizar(
                eq("12345"),
                eq(BigDecimal.valueOf(100.00)),
                eq(Tipo.DEBITO)
        );
    }

    @Test
    @DisplayName("PUT /api/saldos/{contaId} - Deve atualizar saldo com crédito")
    void deveAtualizarSaldoComCredito() throws Exception {
        // Arrange
        atualizarSaldoRequest = new AtualizarSaldoRequest(
                BigDecimal.valueOf(100.00),
                Tipo.CREDITO
        );
        saldo.setCreditoUtilizado(BigDecimal.valueOf(100.00));

        when(saldoService.atualizar(anyString(), any(BigDecimal.class), any(Tipo.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(put("/api/saldos/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarSaldoRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditoUtilizado").value(100.00));

        verify(saldoService, times(1)).atualizar(
                eq("12345"),
                eq(BigDecimal.valueOf(100.00)),
                eq(Tipo.CREDITO)
        );
    }

    @Test
    @DisplayName("PUT /api/saldos/{contaId} - Deve validar valor obrigatório")
    void deveValidarValorObrigatorioNaAtualizacao() throws Exception {
        // Arrange
        atualizarSaldoRequest = new AtualizarSaldoRequest(null, Tipo.DEBITO);

        // Act & Assert
        mockMvc.perform(put("/api/saldos/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarSaldoRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).atualizar(anyString(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/saldos/{contaId} - Deve validar tipo obrigatório")
    void deveValidarTipoObrigatorioNaAtualizacao() throws Exception {
        // Arrange
        atualizarSaldoRequest = new AtualizarSaldoRequest(BigDecimal.valueOf(100.00), null);

        // Act & Assert
        mockMvc.perform(put("/api/saldos/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarSaldoRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).atualizar(anyString(), any(), any());
    }

    @Test
    @DisplayName("PUT /api/saldos/{contaId} - Deve validar valor positivo")
    void deveValidarValorPositivoNaAtualizacao() throws Exception {
        // Arrange
        atualizarSaldoRequest = new AtualizarSaldoRequest(BigDecimal.valueOf(-100.00), Tipo.DEBITO);

        // Act & Assert
        mockMvc.perform(put("/api/saldos/12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atualizarSaldoRequest)))
                .andExpect(status().isBadRequest());

        verify(saldoService, never()).atualizar(anyString(), any(), any());
    }

    @Test
    @DisplayName("POST /api/saldos - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJson() throws Exception {
        // Arrange
        when(saldoService.criarConta(anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("GET /api/saldos/{contaId} - Deve retornar Content-Type application/json")
    void deveRetornarContentTypeJsonNoGet() throws Exception {
        // Arrange
        when(saldoService.buscarSaldo(anyString())).thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(get("/api/saldos/12345"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/saldos - Deve criar conta com valores decimais")
    void deveCriarContaComValoresDecimais() throws Exception {
        // Arrange
        criarContaRequest.setSaldoInicial(new BigDecimal("1234.56"));
        criarContaRequest.setLimiteCredito(new BigDecimal("789.12"));

        when(saldoService.criarConta(anyString(), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(post("/api/saldos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarContaRequest)))
                .andExpect(status().isCreated());

        verify(saldoService, times(1)).criarConta(
                eq("12345"),
                eq(new BigDecimal("1234.56")),
                eq(new BigDecimal("789.12"))
        );
    }

    @Test
    @DisplayName("GET /api/saldos/{contaId} - Deve aceitar contaId com caracteres especiais")
    void deveAceitarContaIdComCaracteresEspeciais() throws Exception {
        // Arrange
        when(saldoService.buscarSaldo(anyString())).thenReturn(saldo);

        // Act & Assert
        mockMvc.perform(get("/api/saldos/ABC-123_XYZ"))
                .andExpect(status().isOk());

        verify(saldoService, times(1)).buscarSaldo("ABC-123_XYZ");
    }
}