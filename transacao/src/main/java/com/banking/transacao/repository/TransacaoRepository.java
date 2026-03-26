package com.banking.transacao.repository;

import com.banking.transacao.model.Transacao;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransacaoRepository extends MongoRepository<Transacao, String> {
}
