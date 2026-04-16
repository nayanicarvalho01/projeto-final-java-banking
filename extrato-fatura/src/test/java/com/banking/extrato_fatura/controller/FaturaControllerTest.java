package com.banking.extrato_fatura.controller;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.service.FaturaService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FaturaController")
class FaturaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FaturaService faturaService;

    @Mock
    private PdfService pdfService;

    @InjectMocks
    private FaturaController faturaController;

    private final String contaId = "12345";
    private final String faturaId = "fat-001";
    private final YearMonth mesReferencia = YearMonth.of(2024, 4);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(faturaController).build();
    }

    @Test
    @DisplayName("GET /api/faturas/{id} - Deve buscar fatura do mês atual por padrão")
    void deveBuscarFaturaMesAtual() throws Exception {
        Fatura fatura = Fatura.builder().contaId(contaId).mesReferencia(YearMonth.now()).build();
        when(faturaService.buscarPorMes(eq(contaId), any(YearMonth.class))).thenReturn(fatura);

        mockMvc.perform(get("/api/faturas/" + contaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contaId").value(contaId));
    }

    @Test
    @DisplayName("GET /api/faturas/{id}/periodo - Deve buscar faturas por período")
    void deveBuscarPorPeriodo() throws Exception {
        YearMonth inicio = YearMonth.of(2024, 1);
        YearMonth fim = YearMonth.of(2024, 3);
        when(faturaService.buscarPorPeriodo(contaId, inicio, fim))
                .thenReturn(List.of(new Fatura(), new Fatura()));

        mockMvc.perform(get("/api/faturas/" + contaId + "/periodo")
                        .param("inicio", "2024-01")
                        .param("fim", "2024-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/faturas/{id}/status - Deve buscar faturas por status")
    void deveBuscarPorStatus() throws Exception {
        when(faturaService.buscarPorStatus(contaId, StatusFatura.ABERTA))
                .thenReturn(List.of(new Fatura()));

        mockMvc.perform(get("/api/faturas/" + contaId + "/status")
                        .param("status", "ABERTA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/faturas/{id}/historico - Deve retornar todas as faturas")
    void deveBuscarTodas() throws Exception {
        when(faturaService.buscarTodas(contaId)).thenReturn(List.of(new Fatura()));

        mockMvc.perform(get("/api/faturas/" + contaId + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/faturas/{id}/pdf - Deve retornar PDF da fatura")
    void deveGerarPdfFatura() throws Exception {
        Fatura fatura = Fatura.builder().contaId(contaId).mesReferencia(mesReferencia).build();
        byte[] pdfFake = "pdf-content".getBytes();

        when(faturaService.buscarPorMes(eq(contaId), any(YearMonth.class))).thenReturn(fatura);
        when(pdfService.gerarPdfFatura(fatura)).thenReturn(pdfFake);

        mockMvc.perform(get("/api/faturas/" + contaId + "/pdf")
                        .param("mes", "2024-04"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("fatura-12345-2024-04.pdf")))
                .andExpect(content().bytes(pdfFake));
    }

    @Test
    @DisplayName("PUT /api/faturas/{id}/fechar - Deve fechar fatura e retornar 204")
    void deveFecharFatura() throws Exception {
        doNothing().when(faturaService).fecharFatura(faturaId);

        mockMvc.perform(put("/api/faturas/" + faturaId + "/fechar"))
                .andExpect(status().isNoContent());

        verify(faturaService, times(1)).fecharFatura(faturaId);
    }

    @Test
    @DisplayName("PUT /api/faturas/{id}/pagar - Deve pagar fatura e retornar 204")
    void devePagarFatura() throws Exception {
        doNothing().when(faturaService).pagarFatura(faturaId);

        mockMvc.perform(put("/api/faturas/" + faturaId + "/pagar"))
                .andExpect(status().isNoContent());

        verify(faturaService, times(1)).pagarFatura(faturaId);
    }
}