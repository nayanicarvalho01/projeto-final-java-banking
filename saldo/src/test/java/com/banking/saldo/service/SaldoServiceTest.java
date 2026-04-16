package com.banking.saldo.service;

import com.banking.saldo.model.Saldo;
import com.banking.saldo.model.Tipo;
import com.banking.saldo.repository.SaldoMongoRepository;
import com.banking.saldo.repository.SaldoRedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SaldoService")
class SaldoServiceTest {

    @Mock
    private SaldoMongoRepository mongoRepository;

    @Mock
    private SaldoRedisRepository redisRepository;

    @InjectMocks
    private SaldoService saldoService;

    private Saldo saldo;

    @BeforeEach
    void setUp() {
        saldo = Saldo.builder()
                .contaId("12345")
                .saldoDebito(BigDecimal.valueOf(1000.00))
                .limiteCredito(BigDecimal.valueOf(500.00))
                .creditoUtilizado(BigDecimal.ZERO)
                .ultimaAtualizacao(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Deve criar conta com sucesso")
    void deveCriarContaComSucesso() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(false);
        doNothing().when(redisRepository).inicializar(anyString(), anyLong(), anyLong());
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.criarConta(
                "12345",
                BigDecimal.valueOf(1000.00),
                BigDecimal.valueOf(500.00)
        );

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContaId()).isEqualTo("12345");
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(resultado.getLimiteCredito()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(resultado.getCreditoUtilizado()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(mongoRepository, times(1)).existsByContaId("12345");
        verify(redisRepository, times(1)).inicializar("12345", 100000L, 50000L);
        verify(mongoRepository, times(1)).save(any(Saldo.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar conta já existente")
    void deveLancarExcecaoAoCriarContaJaExistente() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> saldoService.criarConta(
                "12345",
                BigDecimal.valueOf(1000.00),
                BigDecimal.valueOf(500.00)
        ))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta já existe: 12345");

        verify(mongoRepository, times(1)).existsByContaId("12345");
        verify(redisRepository, never()).inicializar(anyString(), anyLong(), anyLong());
        verify(mongoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve converter valores para centavos corretamente")
    void deveConverterValoresParaCentavosCorretamente() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(false);
        doNothing().when(redisRepository).inicializar(anyString(), anyLong(), anyLong());
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        saldoService.criarConta("12345", new BigDecimal("99.99"), new BigDecimal("50.50"));

        // Assert
        verify(redisRepository, times(1)).inicializar("12345", 9999L, 5050L);
    }

    @Test
    @DisplayName("Deve depositar valor com sucesso")
    void deveDepositarValorComSucesso() {
        // Arrange
        when(redisRepository.depositar("12345", 20000L)).thenReturn(120000L);
        when(redisRepository.getSaldoDebito("12345")).thenReturn(120000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(50000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.depositar("12345", BigDecimal.valueOf(200.00));

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContaId()).isEqualTo("12345");
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(BigDecimal.valueOf(1200.00));

        verify(redisRepository, times(1)).depositar("12345", 20000L);
        verify(mongoRepository, atLeast(2)).findByContaId("12345");
        verify(mongoRepository, times(1)).save(any(Saldo.class));
    }

    @Test
    @DisplayName("Deve atualizar saldo com débito")
    void deveAtualizarSaldoComDebito() {
        // Arrange
        when(redisRepository.debitar("12345", 10000L)).thenReturn(90000L);
        when(redisRepository.getSaldoDebito("12345")).thenReturn(90000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(50000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.atualizar("12345", BigDecimal.valueOf(100.00), Tipo.DEBITO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(BigDecimal.valueOf(900.00));

        verify(redisRepository, times(1)).debitar("12345", 10000L);
        verify(redisRepository, never()).creditar(anyString(), anyLong());
        verify(mongoRepository, times(1)).save(any(Saldo.class));
    }

    @Test
    @DisplayName("Deve atualizar saldo com crédito")
    void deveAtualizarSaldoComCredito() {
        // Arrange
        when(redisRepository.creditar("12345", 10000L)).thenReturn(40000L);
        when(redisRepository.getSaldoDebito("12345")).thenReturn(100000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(40000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.atualizar("12345", BigDecimal.valueOf(100.00), Tipo.CREDITO);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getCreditoUtilizado()).isEqualByComparingTo(BigDecimal.valueOf(100.00));

        verify(redisRepository, times(1)).creditar("12345", 10000L);
        verify(redisRepository, never()).debitar(anyString(), anyLong());
        verify(mongoRepository, times(1)).save(any(Saldo.class));
    }

    @Test
    @DisplayName("Deve buscar saldo com sucesso")
    void deveBuscarSaldoComSucesso() {
        // Arrange
        when(redisRepository.getSaldoDebito("12345")).thenReturn(100000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(50000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));

        // Act
        Saldo resultado = saldoService.buscarSaldo("12345");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getContaId()).isEqualTo("12345");
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(BigDecimal.valueOf(1000.00));
        assertThat(resultado.getLimiteCredito()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(resultado.getCreditoUtilizado()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(redisRepository, times(1)).getSaldoDebito("12345");
        verify(redisRepository, times(1)).getCreditoDisponivel("12345");
        verify(mongoRepository, times(1)).findByContaId("12345");
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar conta inexistente")
    void deveLancarExcecaoAoBuscarContaInexistente() {
        // Arrange
        when(redisRepository.getSaldoDebito("99999")).thenReturn(0L);
        when(redisRepository.getCreditoDisponivel("99999")).thenReturn(0L);
        when(mongoRepository.findByContaId("99999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> saldoService.buscarSaldo("99999"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada: 99999");

        verify(mongoRepository, times(1)).findByContaId("99999");
    }

    @Test
    @DisplayName("Deve calcular crédito utilizado corretamente")
    void deveCalcularCreditoUtilizadoCorretamente() {
        // Arrange
        when(redisRepository.getSaldoDebito("12345")).thenReturn(100000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(30000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));

        // Act
        Saldo resultado = saldoService.buscarSaldo("12345");

        // Assert
        assertThat(resultado.getCreditoUtilizado()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
    }

    @Test
    @DisplayName("Deve converter centavos para reais corretamente")
    void deveConverterCentavosParaReaisCorretamente() {
        // Arrange
        when(redisRepository.getSaldoDebito("12345")).thenReturn(9999L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(5050L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));

        // Act
        Saldo resultado = saldoService.buscarSaldo("12345");

        // Assert
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Deve sincronizar MongoDB após depósito")
    void deveSincronizarMongoDBAposDeposito() {
        // Arrange
        when(redisRepository.depositar("12345", 20000L)).thenReturn(120000L);
        when(redisRepository.getSaldoDebito("12345")).thenReturn(120000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(50000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        saldoService.depositar("12345", BigDecimal.valueOf(200.00));

        // Assert
        verify(mongoRepository, times(1)).save(argThat(s ->
                s.getSaldoDebito().compareTo(BigDecimal.valueOf(1200.00)) == 0
        ));
    }

    @Test
    @DisplayName("Deve atualizar timestamp de última atualização")
    void deveAtualizarTimestampUltimaAtualizacao() {
        // Arrange
        when(redisRepository.getSaldoDebito("12345")).thenReturn(100000L);
        when(redisRepository.getCreditoDisponivel("12345")).thenReturn(50000L);
        when(mongoRepository.findByContaId("12345")).thenReturn(Optional.of(saldo));

        // Act
        Saldo resultado = saldoService.buscarSaldo("12345");

        // Assert
        assertThat(resultado.getUltimaAtualizacao()).isNotNull();
        assertThat(resultado.getUltimaAtualizacao()).isBeforeOrEqualTo(Instant.now());
    }

    @Test
    @DisplayName("Deve criar conta com saldo inicial zero")
    void deveCriarContaComSaldoInicialZero() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(false);
        doNothing().when(redisRepository).inicializar(anyString(), anyLong(), anyLong());
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.criarConta(
                "12345",
                BigDecimal.ZERO,
                BigDecimal.valueOf(500.00)
        );

        // Assert
        assertThat(resultado.getSaldoDebito()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(redisRepository, times(1)).inicializar("12345", 0L, 50000L);
    }

    @Test
    @DisplayName("Deve processar valores com casas decimais")
    void deveProcessarValoresComCasasDecimais() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(false);
        doNothing().when(redisRepository).inicializar(anyString(), anyLong(), anyLong());
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        saldoService.criarConta(
                "12345",
                new BigDecimal("1234.56"),
                new BigDecimal("789.12")
        );

        // Assert
        verify(redisRepository, times(1)).inicializar("12345", 123456L, 78912L);
    }

    @Test
    @DisplayName("Deve criar conta com limite de crédito zero")
    void deveCriarContaComLimiteCreditoZero() {
        // Arrange
        when(mongoRepository.existsByContaId("12345")).thenReturn(false);
        doNothing().when(redisRepository).inicializar(anyString(), anyLong(), anyLong());
        when(mongoRepository.save(any(Saldo.class))).thenReturn(saldo);

        // Act
        Saldo resultado = saldoService.criarConta(
                "12345",
                BigDecimal.valueOf(1000.00),
                BigDecimal.ZERO
        );

        // Assert
        assertThat(resultado.getLimiteCredito()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(redisRepository, times(1)).inicializar("12345", 100000L, 0L);
    }
}