package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.model.ItemExtrato;
import com.banking.extrato_fatura.model.ItemFatura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do PdfService - Geração de iText7")
class PdfServiceTest {

    @InjectMocks
    private PdfService pdfService;

    private Extrato extratoValido;
    private Fatura faturaValida;

    @BeforeEach
    void setUp() {
        // Mock Item Extrato
        ItemExtrato itemExtrato = ItemExtrato.builder()
                .dataHora(Instant.now())
                .comerciante("Loja de Teste")
                .localizacao("São Paulo, SP")
                .valor(new BigDecimal("150.50"))
                .build();

        extratoValido = Extrato.builder()
                .contaId("12345")
                .mesReferencia(YearMonth.of(2024, 4))
                .itens(List.of(itemExtrato))
                .build();

        // Mock Item Fatura
        ItemFatura itemFatura = ItemFatura.builder()
                .dataHora(Instant.now())
                .comerciante("Restaurante Teste")
                .localizacao("Rio de Janeiro, RJ")
                .valor(new BigDecimal("80.00"))
                .build();

        faturaValida = Fatura.builder()
                .contaId("12345")
                .mesReferencia(YearMonth.of(2024, 4))
                .dataVencimento(LocalDate.of(2024, 5, 10))
                .status(StatusFatura.ABERTA) // ✅ CORRIGIDO: Usando o Enum StatusFatura
                .valorTotal(new BigDecimal("80.00"))
                .itens(List.of(itemFatura))
                .build();
    }

    @Test
    @DisplayName("Deve gerar PDF de extrato com sucesso")
    void deveGerarPdfExtratoComSucesso() {
        byte[] pdfBytes = pdfService.gerarPdfExtrato(extratoValido);
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes)).contains("%PDF");
    }

    @Test
    @DisplayName("Deve gerar PDF de fatura com sucesso")
    void deveGerarPdfFaturaComSucesso() {
        byte[] pdfBytes = pdfService.gerarPdfFatura(faturaValida);
        assertThat(pdfBytes).isNotEmpty();
        assertThat(new String(pdfBytes)).contains("%PDF");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar gerar PDF de extrato nulo")
    void deveLancarExcecaoParaExtratoNulo() {
        assertThatThrownBy(() -> pdfService.gerarPdfExtrato(null))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar gerar PDF de fatura nulo")
    void deveLancarExcecaoParaFaturaNulo() {
        assertThatThrownBy(() -> pdfService.gerarPdfFatura(null))
                .isInstanceOf(RuntimeException.class);
    }
}