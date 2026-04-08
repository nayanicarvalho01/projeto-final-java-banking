package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.Tipo;
import com.banking.saldo.repository.SaldoMongoRepository;
import com.banking.saldo.repository.SaldoRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaldoService {

    private final SaldoMongoRepository mongoRepository;
    private final SaldoRedisRepository redisRepository;

    public Saldo criarConta(String contaId, BigDecimal saldoInicial, BigDecimal limiteCredito) {
        log.info("Criando conta: {}", contaId);

        if (mongoRepository.existsByContaId(contaId)) {
            throw new RuntimeException("Conta já existe: " + contaId);
        }

        Long saldoCentavos = toCentavos(saldoInicial);
        Long limiteCentavos = toCentavos(limiteCredito);

        redisRepository.inicializar(contaId, saldoCentavos, limiteCentavos);

        Saldo saldo = Saldo.builder()
                .contaId(contaId)
                .saldoDebito(saldoInicial)
                .limiteCredito(limiteCredito)
                .creditoUtilizado(BigDecimal.ZERO)
                .ultimaAtualizacao(Instant.now())
                .build();

        mongoRepository.save(saldo);

        log.info("Conta criada: {}", contaId);
        return saldo;
    }

    public Saldo depositar(String contaId, BigDecimal valor) {
        log.info("Depositando {} na conta {}", valor, contaId);

        Long centavos = toCentavos(valor);
        redisRepository.depositar(contaId, centavos);

        atualizarMongoDB(contaId);

        return buscarSaldo(contaId);
    }

    public Saldo atualizar(String contaId, BigDecimal valor, Tipo tipo) {
        log.info("Atualizando saldo - Conta: {}, Tipo: {}", contaId, tipo);

        Long centavos = toCentavos(valor);

        if (tipo == Tipo.DEBITO) {
            redisRepository.debitar(contaId, centavos);
        } else {
            redisRepository.creditar(contaId, centavos);
        }

        atualizarMongoDB(contaId);

        return buscarSaldo(contaId);
    }

    public Saldo buscarSaldo(String contaId) {
        Long saldoCentavos = redisRepository.getSaldoDebito(contaId);
        Long creditoDisponivelCentavos = redisRepository.getCreditoDisponivel(contaId);

        BigDecimal saldoDebito = toReais(saldoCentavos);
        BigDecimal creditoDisponivel = toReais(creditoDisponivelCentavos);

        Saldo saldoMongo = mongoRepository.findByContaId(contaId)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada: " + contaId));

        BigDecimal limiteCredito = saldoMongo.getLimiteCredito();
        BigDecimal creditoUtilizado = limiteCredito.subtract(creditoDisponivel);

        return Saldo.builder()
                .contaId(contaId)
                .saldoDebito(saldoDebito)
                .limiteCredito(limiteCredito)
                .creditoUtilizado(creditoUtilizado)
                .ultimaAtualizacao(Instant.now())
                .build();
    }

    private void atualizarMongoDB(String contaId) {
        try {
            Saldo saldoAtual = buscarSaldo(contaId);

            Saldo saldoMongo = mongoRepository.findByContaId(contaId)
                    .orElseThrow(() -> new RuntimeException("Conta não encontrada no MongoDB"));

            saldoMongo.setSaldoDebito(saldoAtual.getSaldoDebito());
            saldoMongo.setCreditoUtilizado(saldoAtual.getCreditoUtilizado());
            saldoMongo.setUltimaAtualizacao(Instant.now());

            mongoRepository.save(saldoMongo);

        } catch (Exception e) {
            log.error("Erro ao sincronizar MongoDB: {}", e.getMessage());
        }
    }

    private Long toCentavos(BigDecimal valor) {
        return valor.multiply(new BigDecimal("100")).longValue();
    }

    private BigDecimal toReais(Long centavos) {
        return new BigDecimal(centavos).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}