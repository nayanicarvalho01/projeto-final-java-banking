package com.banking.saldo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do SaldoRedisRepository")
class SaldoRedisRepositoryTest {

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOperations;

    @InjectMocks
    private SaldoRedisRepository saldoRedisRepository;

    private final String contaId = "12345";

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Deve inicializar conta no Redis")
    void deveInicializarConta() {
        saldoRedisRepository.inicializar(contaId, 1000L, 500L);
        verify(valueOperations).set("conta:12345:debito", 1000L);
        verify(valueOperations).set("conta:12345:credito", 500L);
    }

    @Test
    @DisplayName("Deve depositar com sucesso")
    void deveDepositar() {
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1200L);
        Long novoSaldo = saldoRedisRepository.depositar(contaId, 200L);
        assertThat(novoSaldo).isEqualTo(1200L);
    }

    @Test
    @DisplayName("Deve debitar com sucesso")
    void deveDebitarComSucesso() {
        when(valueOperations.decrement(anyString(), anyLong())).thenReturn(800L);
        Long novoSaldo = saldoRedisRepository.debitar(contaId, 200L);
        assertThat(novoSaldo).isEqualTo(800L);
    }

    @Test
    @DisplayName("Deve lançar erro ao debitar e saldo ficar negativo")
    void deveLancarErroDebitoInsuficiente() {
        when(valueOperations.decrement(anyString(), anyLong())).thenReturn(-50L);

        assertThatThrownBy(() -> saldoRedisRepository.debitar(contaId, 200L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Saldo insuficiente");

        // Verifica se o Redis tentou reverter o valor (incrementar de volta)
        verify(valueOperations).increment(anyString(), eq(200L));
    }

    @Test
    @DisplayName("Deve creditar com sucesso")
    void deveCreditarComSucesso() {
        when(valueOperations.decrement(anyString(), anyLong())).thenReturn(300L);
        Long novoLimite = saldoRedisRepository.creditar(contaId, 200L);
        assertThat(novoLimite).isEqualTo(300L);
    }

    @Test
    @DisplayName("Deve lançar erro ao usar crédito e limite ser insuficiente")
    void deveLancarErroLimiteInsuficiente() {
        when(valueOperations.decrement(anyString(), anyLong())).thenReturn(-10L);

        assertThatThrownBy(() -> saldoRedisRepository.creditar(contaId, 200L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Limite insuficiente");

        verify(valueOperations).increment(anyString(), eq(200L));
    }

    @Test
    @DisplayName("Deve buscar saldo de débito")
    void deveBuscarSaldoDebito() {
        when(valueOperations.get("conta:12345:debito")).thenReturn(1000L);
        Long saldo = saldoRedisRepository.getSaldoDebito(contaId);
        assertThat(saldo).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Deve buscar crédito disponível")
    void deveBuscarCreditoDisponivel() {
        when(valueOperations.get("conta:12345:credito")).thenReturn(500L);
        Long credito = saldoRedisRepository.getCreditoDisponivel(contaId);
        assertThat(credito).isEqualTo(500L);
    }

    @Test
    @DisplayName("Deve lançar erro quando conta não existe no Redis")
    void deveLancarErroContaNaoEncontrada() {
        when(valueOperations.get(anyString())).thenReturn(null);
        assertThatThrownBy(() -> saldoRedisRepository.getSaldoDebito(contaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Conta não encontrada");
    }
}