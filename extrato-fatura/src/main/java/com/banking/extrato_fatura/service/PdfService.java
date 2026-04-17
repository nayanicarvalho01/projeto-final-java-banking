package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.model.ItemExtrato;
import com.banking.extrato_fatura.model.ItemFatura;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] gerarPdfExtrato(Extrato extrato) {
        log.info("Gerando PDF de extrato - Conta: {}, Mês: {}", extrato.getContaId(), extrato.getMesReferencia());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("EXTRATO BANCÁRIO")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Conta: " + extrato.getContaId())
                    .setFontSize(12));
            document.add(new Paragraph("Período: " + extrato.getMesReferencia())
                    .setFontSize(12)
                    .setMarginBottom(20));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}))
                    .useAllAvailableWidth();

            table.addHeaderCell("Data/Hora");
            table.addHeaderCell("Comerciante");
            table.addHeaderCell("Localização");
            table.addHeaderCell("Valor");

            for (ItemExtrato item : extrato.getItens()) {
                table.addCell(item.getDataHora().atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                        .format(DATE_FORMATTER));
                table.addCell(item.getComerciante());
                table.addCell(item.getLocalizacao());
                table.addCell("R$ " + item.getValor().toString());
            }

            document.add(table);

            BigDecimal total = extrato.getItens().stream()
                    .map(ItemExtrato::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            document.add(new Paragraph("Total de débitos: R$ " + total)
                    .setFontSize(12)
                    .setBold()
                    .setMarginTop(20));

            document.close();

            log.info("PDF de extrato gerado - Conta: {}, Tamanho: {} bytes",
                    extrato.getContaId(), baos.size());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF de extrato - Conta: {}", extrato.getContaId(), e);
            throw new RuntimeException("Erro ao gerar PDF de extrato", e);
        }
    }

    public byte[] gerarPdfFatura(Fatura fatura) {
        log.info("Gerando PDF de fatura - Conta: {}, Mês: {}", fatura.getContaId(), fatura.getMesReferencia());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("FATURA DO CARTÃO DE CRÉDITO")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Conta: " + fatura.getContaId())
                    .setFontSize(12));
            document.add(new Paragraph("Período: " + fatura.getMesReferencia())
                    .setFontSize(12));
            document.add(new Paragraph("Vencimento: " + fatura.getDataVencimento())
                    .setFontSize(12));
            document.add(new Paragraph("Status: " + fatura.getStatus())
                    .setFontSize(12)
                    .setMarginBottom(20));

            Table table = new Table(UnitValue.createPercentArray(new float[]{2, 3, 2, 2}))
                    .useAllAvailableWidth();

            table.addHeaderCell("Data/Hora");
            table.addHeaderCell("Comerciante");
            table.addHeaderCell("Localização");
            table.addHeaderCell("Valor");

            for (ItemFatura item : fatura.getItens()) {
                table.addCell(item.getDataHora().atZone(java.time.ZoneId.of("America/Sao_Paulo"))
                        .format(DATE_FORMATTER));
                table.addCell(item.getComerciante());
                table.addCell(item.getLocalizacao());
                table.addCell("R$ " + item.getValor().toString());
            }

            document.add(table);

            document.add(new Paragraph("VALOR TOTAL DA FATURA: R$ " + fatura.getValorTotal())
                    .setFontSize(14)
                    .setBold()
                    .setMarginTop(20)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.close();

            log.info("PDF de fatura gerado - Conta: {}, Tamanho: {} bytes",
                    fatura.getContaId(), baos.size());

            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF de fatura - Conta: {}", fatura.getContaId(), e);
            throw new RuntimeException("Erro ao gerar PDF de fatura", e);
        }
    }
}