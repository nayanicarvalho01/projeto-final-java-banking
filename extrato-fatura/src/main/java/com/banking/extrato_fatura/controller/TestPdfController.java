package com.banking.extrato_fatura.controller;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.model.ItemExtrato;
import com.banking.extrato_fatura.model.ItemFatura;
import com.banking.extrato_fatura.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestPdfController {

    private final PdfService pdfService;

    @GetMapping("/extrato/pdf")
    public ResponseEntity<byte[]> testarPdfExtrato() {
        Extrato extrato = Extrato.builder()
                .contaId("123")
                .mesReferencia(YearMonth.now())
                .itens(List.of(
                        ItemExtrato.builder()
                                .transacaoId("tx-001")
                                .comerciante("Mercado")
                                .localizacao("São Paulo")
                                .valor(new BigDecimal("50.00"))
                                .dataHora(Instant.now())
                                .build(),
                        ItemExtrato.builder()
                                .transacaoId("tx-002")
                                .comerciante("Farmácia")
                                .localizacao("Rio de Janeiro")
                                .valor(new BigDecimal("30.00"))
                                .dataHora(Instant.now())
                                .build()
                ))
                .build();

        byte[] pdf = pdfService.gerarPdfExtrato(extrato);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato-teste.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/fatura/pdf")
    public ResponseEntity<byte[]> testarPdfFatura() {
        Fatura fatura = Fatura.builder()
                .contaId("123")
                .mesReferencia(YearMonth.now())
                .valorTotal(new BigDecimal("230.00"))
                .dataVencimento(LocalDate.now().plusDays(10))
                .status(StatusFatura.ABERTA)
                .itens(List.of(
                        ItemFatura.builder()
                                .transacaoId("tx-003")
                                .comerciante("Loja Online")
                                .localizacao("E-commerce")
                                .valor(new BigDecimal("150.00"))
                                .dataHora(Instant.now())
                                .build(),
                        ItemFatura.builder()
                                .transacaoId("tx-004")
                                .comerciante("Restaurante")
                                .localizacao("São Paulo")
                                .valor(new BigDecimal("80.00"))
                                .dataHora(Instant.now())
                                .build()
                ))
                .build();

        byte[] pdf = pdfService.gerarPdfFatura(fatura);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"fatura-teste.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}