package com.banking.extrato_fatura.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "extratos")
@CompoundIndex(name = "conta_mes_idx", def = "{'contaId': 1, 'mesReferencia': 1}", unique = true)
public class Extrato {

    @Id
    private String id;

    private String contaId;

    private YearMonth mesReferencia;

    @Builder.Default
    private List<ItemExtrato> itens = new ArrayList<>();
}