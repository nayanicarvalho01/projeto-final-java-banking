package com.banking.frontend.controller;

import com.banking.frontend.dto.SaldoResponse;
import com.banking.frontend.dto.TransacaoResponse;
import com.banking.frontend.service.ExtratoFaturaClientService;
import com.banking.frontend.service.SaldoClientService;
import com.banking.frontend.service.TransacaoClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final SaldoClientService saldoClientService;
    private final TransacaoClientService transacaoClientService;
    private final ExtratoFaturaClientService extratoFaturaClientService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam String contaId, Model model) {
        log.info("Acessando dashboard - Conta: {}", contaId);

        // Buscar saldo
        SaldoResponse saldo = saldoClientService.buscarSaldo(contaId);

        // Buscar transações
        List<TransacaoResponse> transacoes = transacaoClientService.buscarTransacoes(contaId);

        // Ordenar por data (mais recentes primeiro) e limitar a 10
        List<TransacaoResponse> ultimasTransacoes = transacoes.stream()
                .sorted(Comparator.comparing(TransacaoResponse::getDataHora).reversed())
                .limit(10)
                .collect(Collectors.toList());

        // Separar por tipo
        List<TransacaoResponse> transacoesDebito = ultimasTransacoes.stream()
                .filter(t -> t.getTipo() == TransacaoResponse.Tipo.DEBITO)
                .collect(Collectors.toList());

        List<TransacaoResponse> transacoesCredito = ultimasTransacoes.stream()
                .filter(t -> t.getTipo() == TransacaoResponse.Tipo.CREDITO)
                .collect(Collectors.toList());

        model.addAttribute("contaId", contaId);
        model.addAttribute("saldo", saldo);
        model.addAttribute("transacoesDebito", transacoesDebito);
        model.addAttribute("transacoesCredito", transacoesCredito);

        return "dashboard";
    }

    @GetMapping("/dashboard/extrato/download")
    public ResponseEntity<byte[]> downloadExtrato(
            @RequestParam String contaId,
            @RequestParam(required = false) String mes) {

        YearMonth mesReferencia = mes != null ? YearMonth.parse(mes) : YearMonth.now();

        log.info("Download extrato - Conta: {}, Mês: {}", contaId, mesReferencia);

        byte[] pdf = extratoFaturaClientService.gerarExtratoPdf(contaId, mesReferencia);

        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = String.format("extrato-%s-%s.pdf", contaId, mesReferencia);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/dashboard/fatura/download")
    public ResponseEntity<byte[]> downloadFatura(
            @RequestParam String contaId,
            @RequestParam(required = false) String mes) {

        YearMonth mesReferencia = mes != null ? YearMonth.parse(mes) : YearMonth.now();

        log.info("Download fatura - Conta: {}, Mês: {}", contaId, mesReferencia);

        byte[] pdf = extratoFaturaClientService.gerarFaturaPdf(contaId, mesReferencia);

        if (pdf == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = String.format("fatura-%s-%s.pdf", contaId, mesReferencia);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}