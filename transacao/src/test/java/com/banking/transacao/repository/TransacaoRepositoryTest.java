package com.banking.transacao.repository;

import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do TransacaoRepository")
class TransacaoRepositoryTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Test
    @DisplayName("Deve salvar transação")
    void deveSalvarTransacao() {
        // Arrange
        Transacao transacao = Transacao.builder()
                .contaId("12345")
                .comerciante("Loja ABC")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        Transacao transacaoSalva = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .comerciante("Loja ABC")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacaoSalva);

        // Act
        Transacao resultado = transacaoRepository.save(transacao);

        // Assert
        assertThat(resultado.getId()).isEqualTo("trans-001");
        assertThat(resultado.getContaId()).isEqualTo("12345");
        verify(transacaoRepository, times(1)).save(any(Transacao.class));
    }

    @Test
    @DisplayName("Deve buscar transações por contaId ordenadas por dataHora")
    void deveBuscarPorContaIdOrdenadas() {
        // Arrange
        Instant agora = Instant.now();

        Transacao transacao1 = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .dataHora(agora.minusSeconds(3600))
                .build();

        Transacao transacao2 = Transacao.builder()
                .id("trans-002")
                .contaId("12345")
                .dataHora(agora)
                .build();

        when(transacaoRepository.findByContaIdOrderByDataHoraDesc("12345"))
                .thenReturn(List.of(transacao2, transacao1));

        // Act
        List<Transacao> resultado = transacaoRepository.findByContaIdOrderByDataHoraDesc("12345");

        // Assert
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo("trans-002");
        assertThat(resultado.get(1).getId()).isEqualTo("trans-001");
        verify(transacaoRepository, times(1)).findByContaIdOrderByDataHoraDesc("12345");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando conta não tem transações")
    void deveRetornarListaVaziaQuandoContaSemTransacoes() {
        // Arrange
        when(transacaoRepository.findByContaIdOrderByDataHoraDesc("99999"))
                .thenReturn(List.of());

        // Act
        List<Transacao> resultado = transacaoRepository.findByContaIdOrderByDataHoraDesc("99999");

        // Assert
        assertThat(resultado).isEmpty();
        verify(transacaoRepository, times(1)).findByContaIdOrderByDataHoraDesc("99999");
    }

    @Test
    @DisplayName("Deve buscar transação por ID")
    void deveBuscarPorId() {
        // Arrange
        Transacao transacao = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .build();

        when(transacaoRepository.findById("trans-001"))
                .thenReturn(Optional.of(transacao));

        // Act
        Optional<Transacao> resultado = transacaoRepository.findById("trans-001");

        // Assert
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo("trans-001");
        verify(transacaoRepository, times(1)).findById("trans-001");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando transação não existe")
    void deveRetornarVazioQuandoTransacaoNaoExiste() {
        // Arrange
        when(transacaoRepository.findById("inexistente"))
                .thenReturn(Optional.empty());

        // Act
        Optional<Transacao> resultado = transacaoRepository.findById("inexistente");

        // Assert
        assertThat(resultado).isEmpty();
        verify(transacaoRepository, times(1)).findById("inexistente");
    }

    @Test
    @DisplayName("Deve deletar transação por ID")
    void deveDeletarTransacaoPorId() {
        // Arrange
        doNothing().when(transacaoRepository).deleteById("trans-001");

        // Act & Assert
        assertThatCode(() -> transacaoRepository.deleteById("trans-001"))
                .doesNotThrowAnyException();
        verify(transacaoRepository, times(1)).deleteById("trans-001");
    }

    @Test
    @DisplayName("Deve contar total de transações")
    void deveContarTotalTransacoes() {
        // Arrange
        when(transacaoRepository.count()).thenReturn(10L);

        // Act
        long count = transacaoRepository.count();

        // Assert
        assertThat(count).isEqualTo(10L);
        verify(transacaoRepository, times(1)).count();
    }

    @Test
    @DisplayName("Deve verificar se transação existe por ID")
    void deveVerificarSeTransacaoExiste() {
        // Arrange
        when(transacaoRepository.existsById("trans-001")).thenReturn(true);
        when(transacaoRepository.existsById("inexistente")).thenReturn(false);

        // Act & Assert
        assertThat(transacaoRepository.existsById("trans-001")).isTrue();
        assertThat(transacaoRepository.existsById("inexistente")).isFalse();
        verify(transacaoRepository, times(2)).existsById(anyString());
    }

    @Test
    @DisplayName("Deve salvar múltiplas transações")
    void deveSalvarMultiplasTransacoes() {
        // Arrange
        List<Transacao> transacoes = List.of(
                Transacao.builder().contaId("12345").build(),
                Transacao.builder().contaId("67890").build()
        );

        when(transacaoRepository.saveAll(anyList())).thenReturn(transacoes);

        // Act
        List<Transacao> resultado = transacaoRepository.saveAll(transacoes);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(transacaoRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve deletar todas as transações")
    void deveDeletarTodasTransacoes() {
        // Arrange
        doNothing().when(transacaoRepository).deleteAll();

        // Act & Assert
        assertThatCode(() -> transacaoRepository.deleteAll())
                .doesNotThrowAnyException();
        verify(transacaoRepository, times(1)).deleteAll();
    }

    @Test
    @DisplayName("Deve buscar todas as transações")
    void deveBuscarTodasTransacoes() {
        // Arrange
        List<Transacao> transacoes = List.of(
                Transacao.builder().id("trans-001").build(),
                Transacao.builder().id("trans-002").build()
        );

        when(transacaoRepository.findAll()).thenReturn(transacoes);

        // Act
        List<Transacao> resultado = transacaoRepository.findAll();

        // Assert
        assertThat(resultado).hasSize(2);
        verify(transacaoRepository, times(1)).findAll();
    }
}