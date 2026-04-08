package com.banking.extrato_fatura.controller;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.service.FaturaService;
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
@RequestMapping("/api/faturas")
@RequiredArgsConstructor
public class FaturaController {

    private final FaturaService faturaService;
    private final PdfService pdfService;

    @GetMapping("/{contaId}")
    public ResponseEntity<Fatura> buscarPorMes(
            @PathVariable String contaId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        YearMonth mesReferencia = mes != null ? mes : YearMonth.now();

        log.info("Buscando fatura - Conta: {}, Mês: {}", contaId, mesReferencia);

        Fatura fatura = faturaService.buscarPorMes(contaId, mesReferencia);

        return ResponseEntity.ok(fatura);
    }

    @GetMapping("/{contaId}/periodo")
    public ResponseEntity<List<Fatura>> buscarPorPeriodo(
            @PathVariable String contaId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth inicio,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth fim) {

        log.info("Buscando faturas - Conta: {}, Período: {} a {}", contaId, inicio, fim);

        List<Fatura> faturas = faturaService.buscarPorPeriodo(contaId, inicio, fim);

        return ResponseEntity.ok(faturas);
    }

    @GetMapping("/{contaId}/status")
    public ResponseEntity<List<Fatura>> buscarPorStatus(
            @PathVariable String contaId,
            @RequestParam StatusFatura status) {

        log.info("Buscando faturas - Conta: {}, Status: {}", contaId, status);

        List<Fatura> faturas = faturaService.buscarPorStatus(contaId, status);

        return ResponseEntity.ok(faturas);
    }

    @GetMapping("/{contaId}/historico")
    public ResponseEntity<List<Fatura>> buscarTodas(@PathVariable String contaId) {

        log.info("Buscando histórico de faturas - Conta: {}", contaId);

        List<Fatura> faturas = faturaService.buscarTodas(contaId);

        return ResponseEntity.ok(faturas);
    }

    @GetMapping("/{contaId}/pdf")
    public ResponseEntity<byte[]> gerarPdf(
            @PathVariable String contaId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes) {

        YearMonth mesReferencia = mes != null ? mes : YearMonth.now();

        log.info("Gerando PDF de fatura - Conta: {}, Mês: {}", contaId, mesReferencia);

        Fatura fatura = faturaService.buscarPorMes(contaId, mesReferencia);
        byte[] pdf = pdfService.gerarPdfFatura(fatura);

        String filename = String.format("fatura-%s-%s.pdf", contaId, mesReferencia);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PutMapping("/{faturaId}/fechar")
    public ResponseEntity<Void> fecharFatura(@PathVariable String faturaId) {

        log.info("Fechando fatura - ID: {}", faturaId);

        faturaService.fecharFatura(faturaId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{faturaId}/pagar")
    public ResponseEntity<Void> pagarFatura(@PathVariable String faturaId) {

        log.info("Pagando fatura - ID: {}", faturaId);

        faturaService.pagarFatura(faturaId);

        return ResponseEntity.noContent().build();
    }
}