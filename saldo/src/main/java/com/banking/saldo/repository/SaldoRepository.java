package com.banking.saldo.repository;

import com.banking.saldo.model.Saldo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaldoRepository extends CrudRepository<Saldo, String> {
}
