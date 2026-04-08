package com.banking.extrato_fatura.controller;

import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.service.ExtratoService;
import com.banking.extrato_fatura.service.PdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/extratos")
@RequiredArgsConstructor
public class ExtratoController {

    private final ExtratoService extratoService;
    private final PdfService pdfService;

    @GetMapping("/{contaId}")
    public ResponseEntity<Extrato> buscarPorMes(
            @PathVariable String contaId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        YearMonth mesReferencia = mes != null ? mes : YearMonth.now();

        log.info("Buscando extrato - Conta: {}, Mês: {}", contaId, mesReferencia);

        Extrato extrato = extratoService.buscarPorMes(contaId, mesReferencia);

        return ResponseEntity.ok(extrato);
    }

    @GetMapping("/{contaId}/periodo")
    public ResponseEntity<List<Extrato>> buscarPorPeriodo(
            @PathVariable String contaId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth fim) {

        log.info("Buscando extratos - Conta: {}, Período: {} a {}", contaId, inicio, fim);

        List<Extrato> extratos = extratoService.buscarPorPeriodo(contaId, inicio, fim);

        return ResponseEntity.ok(extratos);
    }

    @GetMapping("/{contaId}/historico")
    public ResponseEntity<List<Extrato>> buscarTodos(@PathVariable String contaId) {

        log.info("Buscando histórico de extratos - Conta: {}", contaId);

        List<Extrato> extratos = extratoService.buscarTodos(contaId);

        return ResponseEntity.ok(extratos);
    }

    @GetMapping("/{contaId}/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable String contaId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        YearMonth mesReferencia = mes != null ? mes : YearMonth.now();

        log.info("Gerando PDF de extrato - Conta: {}, Mês: {}", contaId, mesReferencia);

        Extrato extrato = extratoService.buscarPorMes(contaId, mesReferencia);
        byte[] pdf = pdfService.gerarPdfExtrato(extrato);

        String filename = String.format("extrato-%s-%s.pdf", contaId, mesReferencia);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}