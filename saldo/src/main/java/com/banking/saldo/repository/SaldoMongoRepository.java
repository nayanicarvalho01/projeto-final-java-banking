package com.banking.saldo.repository;

import com.banking.saldo.model.Saldo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface SaldoMongoRepository extends MongoRepository<Saldo, String> {

    Optional<Saldo> findByContaId(String contaId);

    boolean existsByContaId(String contaId);

}