package com.banking.extrato_fatura.controller;

import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.service.ExtratoService;
import com.banking.extrato_fatura.service.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.YearMonth;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ExtratoController")
class ExtratoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExtratoService extratoService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private ExtratoController extratoController;

    private final String contaId = "12345";
    private final YearMonth mesReferencia = YearMonth.of(2024, 4);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(extratoController).build();
    }

    @Test
    @DisplayName("GET /api/extratos/{id} - Deve retornar extrato do mês atual quando parâmetro for omitido")
    void deveBuscarExtratoMesAtual() throws Exception {
        // Arrange
        Extrato extrato = Extrato.builder().contaId(contaId).mesReferencia(YearMonth.now()).build();
        when(extratoService.buscarPorMes(eq(contaId), any(YearMonth.class))).thenReturn(extrato);

        // Act & Assert
        mockMvc.perform(get("/api/extratos/" + contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value(contaId));

        verify(extratoService).buscarPorMes(eq(contaId), any(YearMonth.class));
    }

    @Test
    @DisplayName("GET /api/extratos/{id} - Deve retornar extrato de um mês específico")
    void deveBuscarExtratoMesEspecifico() throws Exception {
        // Arrange
        Extrato extrato = Extrato.builder().contaId(contaId).mesReferencia(mesReferencia).build();
        when(extratoService.buscarPorMes(contaId, mesReferencia)).thenReturn(extrato);

        // Act & Assert
        mockMvc.perform(get("/api/extratos/" + contaId)
                        .param("mes", "2024-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mesReferencia").value("2024-04"));
    }

    @Test
    @DisplayName("GET /api/extratos/{id}/periodo - Deve retornar lista de extratos no período")
    void deveBuscarPorPeriodo() throws Exception {
        // Arrange
        YearMonth inicio = YearMonth.of(2024, 1);
        YearMonth fim = YearMonth.of(2024, 3);
        when(extratoService.buscarPorPeriodo(contaId, inicio, fim))
                .thenReturn(List.of(new Extrato(), new Extrato()));

        // Act & Assert
        mockMvc.perform(get("/api/extratos/" + contaId + "/periodo")
                        .param("inicio", "2024-01")
                        .param("fim", "2024-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/extratos/{id}/historico - Deve retornar todos os extratos")
    void deveBuscarTodos() throws Exception {
        // Arrange
        when(extratoService.buscarTodos(contaId)).thenReturn(List.of(new Extrato()));

        // Act & Assert
        mockMvc.perform(get("/api/extratos/" + contaId + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/extratos/{id}/pdf - Deve retornar arquivo PDF com headers corretos")
    void deveGerarPdfExtrato() throws Exception {
        // Arrange
        Extrato extrato = Extrato.builder().contaId(contaId).mesReferencia(mesReferencia).build();
        byte[] pdfFake = "conteudo-pdf".getBytes();

        when(extratoService.buscarPorMes(eq(contaId), any(YearMonth.class))).thenReturn(extrato);
        when(pdfService.gerarPdfExtrato(extrato)).thenReturn(pdfFake);

        // Act & Assert
        mockMvc.perform(get("/api/extratos/" + contaId + "/pdf")
                        .param("mes", "2024-04"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("attachment; filename=\"extrato-12345-2024-04.pdf\"")))
                .andExpect(content().bytes(pdfFake));
    }
}