package com.banking.extrato_fatura.model;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "faturas")
@CompoundIndex(name = "conta_mes_idx", def = "{'contaId': 1, 'mesReferencia': 1}", unique = true)
public class Fatura {

    @Id
    private String id;

    private String contaId;

    private YearMonth mesReferencia;  // Ex: 2024-01

    @Builder.Default
    private List<ItemFatura> itens = new ArrayList<>();

    private BigDecimal valorTotal;

    private LocalDate dataVencimento;

    @Builder.Default
    private StatusFatura status = StatusFatura.ABERTA;
}