package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.model.Extrato;
import com.banking.extrato_fatura.repository.ExtratoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do ExtratoService")
class ExtratoServiceTest {

    @Mock
    private ExtratoRepository extratoRepository;

    @InjectMocks
    private ExtratoService extratoService;

    private String contaId = "12345";
    private YearMonth mesAtual = YearMonth.now();

    @Test
    @DisplayName("Deve adicionar item criando um novo extrato quando não existir")
    void deveAdicionarItemCriandoNovoExtrato() {
        // Arrange
        when(extratoRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.empty());

        // Act
        extratoService.adicionarItem(
                contaId, "t-001", "Loja A", "SP",
                new BigDecimal("50.00"), Instant.now()
        );

        // Assert
        verify(extratoRepository, times(1)).save(argThat(extrato -> {
            return extrato.getContaId().equals(contaId) &&
                    extrato.getItens().size() == 1 &&
                    extrato.getItens().get(0).getComerciante().equals("Loja A");
        }));
    }

    @Test
    @DisplayName("Deve adicionar item em um extrato já existente")
    void deveAdicionarItemEmExtratoExistente() {
        // Arrange
        Extrato extratoExistente = Extrato.builder()
                .contaId(contaId)
                .mesReferencia(mesAtual)
                .itens(new ArrayList<>()) // Lista mutável
                .build();

        when(extratoRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.of(extratoExistente));

        // Act
        extratoService.adicionarItem(
                contaId, "t-002", "Loja B", "RJ",
                new BigDecimal("100.00"), Instant.now()
        );

        // Assert
        assertThat(extratoExistente.getItens()).hasSize(1);
        verify(extratoRepository, times(1)).save(extratoExistente);
    }

    @Test
    @DisplayName("Deve buscar extrato por mês com sucesso")
    void deveBuscarPorMesComSucesso() {
        // Arrange
        Extrato extrato = Extrato.builder().contaId(contaId).mesReferencia(mesAtual).build();
        when(extratoRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.of(extrato));

        // Act
        Extrato resultado = extratoService.buscarPorMes(contaId, mesAtual);

        // Assert
        assertThat(resultado).isEqualTo(extrato);
    }

    @Test
    @DisplayName("Deve lançar exceção quando extrato por mês não for encontrado")
    void deveLancarExcecaoQuandoNaoEncontrado() {
        // Arrange
        when(extratoRepository.findByContaIdAndMesReferencia(anyString(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> extratoService.buscarPorMes(contaId, mesAtual))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Extrato não encontrado");
    }

    @Test
    @DisplayName("Deve buscar extratos por período")
    void deveBuscarPorPeriodo() {
        // Arrange
        YearMonth inicio = mesAtual.minusMonths(2);
        YearMonth fim = mesAtual;
        when(extratoRepository.findByContaIdAndMesReferenciaBetween(contaId, inicio, fim))
                .thenReturn(List.of(new Extrato(), new Extrato()));

        // Act
        List<Extrato> resultado = extratoService.buscarPorPeriodo(contaId, inicio, fim);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(extratoRepository, times(1)).findByContaIdAndMesReferenciaBetween(contaId, inicio, fim);
    }

    @Test
    @DisplayName("Deve buscar todos os extratos de uma conta")
    void deveBuscarTodos() {
        // Arrange
        when(extratoRepository.findByContaIdOrderByMesReferenciaDesc(contaId))
                .thenReturn(List.of(new Extrato()));

        // Act
        List<Extrato> resultado = extratoService.buscarTodos(contaId);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(extratoRepository, times(1)).findByContaIdOrderByMesReferenciaDesc(contaId);
    }
}