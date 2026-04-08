package com.banking.extrato_fatura.repository;


import com.banking.extrato_fatura.model.Extrato;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExtratoRepository extends MongoRepository<Extrato, String> {

    Optional<Extrato> findByContaIdAndMesReferencia(String contaId, YearMonth mesReferencia);

    List<Extrato> findByContaIdOrderByMesReferenciaDesc(String contaId);

    List<Extrato> findByContaIdAndMesReferenciaBetween(String contaId, YearMonth inicio, YearMonth fim);
}