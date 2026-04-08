package com.banking.extrato_fatura.repository;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Fatura;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends MongoRepository<Fatura, String> {

    Optional<Fatura> findByContaIdAndMesReferencia(String contaId, YearMonth mesReferencia);

    List<Fatura> findByContaIdOrderByMesReferenciaDesc(String contaId);

    List<Fatura> findByContaIdAndMesReferenciaBetween(String contaId, YearMonth inicio, YearMonth fim);

    List<Fatura> findByContaIdAndStatus(String contaId, StatusFatura status);
}