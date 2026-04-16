package com.banking.extrato_fatura.service;

import com.banking.extrato_fatura.enumerated.StatusFatura;
import com.banking.extrato_fatura.model.Fatura;
import com.banking.extrato_fatura.model.ItemFatura;
import com.banking.extrato_fatura.repository.FaturaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do FaturaService")
class FaturaServiceTest {

    @Mock
    private FaturaRepository faturaRepository;

    @InjectMocks
    private FaturaService faturaService;

    @Captor
    private ArgumentCaptor<Fatura> faturaCaptor;

    private final String contaId = "12345";
    private final String faturaId = "fat-001";
    private final YearMonth mesAtual = YearMonth.now();

    @Test
    @DisplayName("Deve adicionar item criando uma nova fatura quando não existir")
    void deveAdicionarItemCriandoNovaFatura() {
        // Arrange
        when(faturaRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.empty());

        // Act
        faturaService.adicionarItem(
                contaId, "t-001", "Loja A", "SP",
                new BigDecimal("150.00"), Instant.now()
        );

        // Assert
        verify(faturaRepository, times(1)).save(faturaCaptor.capture());
        Fatura faturaSalva = faturaCaptor.getValue();

        assertThat(faturaSalva.getContaId()).isEqualTo(contaId);
        assertThat(faturaSalva.getMesReferencia()).isEqualTo(mesAtual);
        assertThat(faturaSalva.getItens()).hasSize(1);
        assertThat(faturaSalva.getValorTotal()).isEqualByComparingTo(new BigDecimal("150.00"));

        // Verifica se a data de vencimento foi calculada corretamente (+10 dias do fim do mês)
        LocalDate vencimentoEsperado = mesAtual.atEndOfMonth().plusDays(10);
        assertThat(faturaSalva.getDataVencimento()).isEqualTo(vencimentoEsperado);
    }

    @Test
    @DisplayName("Deve adicionar item em uma fatura já existente e recalcular o total")
    void deveAdicionarItemEmFaturaExistente() {
        // Arrange
        ItemFatura itemExistente = ItemFatura.builder()
                .valor(new BigDecimal("50.00"))
                .build();

        List<ItemFatura> itens = new ArrayList<>();
        itens.add(itemExistente);

        Fatura faturaExistente = Fatura.builder()
                .contaId(contaId)
                .mesReferencia(mesAtual)
                .valorTotal(new BigDecimal("50.00"))
                .itens(itens) // Usando ArrayList para permitir adição
                .build();

        when(faturaRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.of(faturaExistente));

        // Act
        faturaService.adicionarItem(
                contaId, "t-002", "Loja B", "RJ",
                new BigDecimal("100.00"), Instant.now()
        );

        // Assert
        verify(faturaRepository, times(1)).save(faturaCaptor.capture());
        Fatura faturaSalva = faturaCaptor.getValue();

        assertThat(faturaSalva.getItens()).hasSize(2);
        // O novo total deve ser 50.00 (antigo) + 100.00 (novo) = 150.00
        assertThat(faturaSalva.getValorTotal()).isEqualByComparingTo(new BigDecimal("150.00"));
    }

    @Test
    @DisplayName("Deve buscar fatura por mês com sucesso")
    void deveBuscarPorMesComSucesso() {
        // Arrange
        Fatura fatura = Fatura.builder().contaId(contaId).mesReferencia(mesAtual).build();
        when(faturaRepository.findByContaIdAndMesReferencia(contaId, mesAtual))
                .thenReturn(Optional.of(fatura));

        // Act
        Fatura resultado = faturaService.buscarPorMes(contaId, mesAtual);

        // Assert
        assertThat(resultado).isEqualTo(fatura);
    }

    @Test
    @DisplayName("Deve lançar exceção quando fatura por mês não for encontrada")
    void deveLancarExcecaoQuandoFaturaNaoEncontradaPorMes() {
        // Arrange
        when(faturaRepository.findByContaIdAndMesReferencia(anyString(), any()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> faturaService.buscarPorMes(contaId, mesAtual))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Fatura não encontrada");
    }

    @Test
    @DisplayName("Deve buscar faturas por período")
    void deveBuscarPorPeriodo() {
        // Arrange
        YearMonth inicio = mesAtual.minusMonths(2);
        YearMonth fim = mesAtual;
        when(faturaRepository.findByContaIdAndMesReferenciaBetween(contaId, inicio, fim))
                .thenReturn(List.of(new Fatura(), new Fatura()));

        // Act
        List<Fatura> resultado = faturaService.buscarPorPeriodo(contaId, inicio, fim);

        // Assert
        assertThat(resultado).hasSize(2);
        verify(faturaRepository, times(1)).findByContaIdAndMesReferenciaBetween(contaId, inicio, fim);
    }

    @Test
    @DisplayName("Deve buscar faturas por status")
    void deveBuscarPorStatus() {
        // Arrange
        when(faturaRepository.findByContaIdAndStatus(contaId, StatusFatura.ABERTA))
                .thenReturn(List.of(new Fatura()));

        // Act
        List<Fatura> resultado = faturaService.buscarPorStatus(contaId, StatusFatura.ABERTA);

        // Assert
        assertThat(resultado).hasSize(1);
        verify(faturaRepository, times(1)).findByContaIdAndStatus(contaId, StatusFatura.ABERTA);
    }

    @Test
    @DisplayName("Deve buscar todas as faturas de uma conta")
    void deveBuscarTodas() {
        // Arrange
        when(faturaRepository.findByContaIdOrderByMesReferenciaDesc(contaId))
                .thenReturn(List.of(new Fatura(), new Fatura(), new Fatura()));

        // Act
        List<Fatura> resultado = faturaService.buscarTodas(contaId);

        // Assert
        assertThat(resultado).hasSize(3);
        verify(faturaRepository, times(1)).findByContaIdOrderByMesReferenciaDesc(contaId);
    }

    @Test
    @DisplayName("Deve fechar fatura com sucesso")
    void deveFecharFaturaComSucesso() {
        // Arrange
        Fatura fatura = Fatura.builder()
                .id(faturaId)
                .status(StatusFatura.ABERTA)
                .build();
        when(faturaRepository.findById(faturaId)).thenReturn(Optional.of(fatura));

        // Act
        faturaService.fecharFatura(faturaId);

        // Assert
        verify(faturaRepository, times(1)).save(faturaCaptor.capture());
        assertThat(faturaCaptor.getValue().getStatus()).isEqualTo(StatusFatura.FECHADA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar fechar fatura inexistente")
    void deveLancarExcecaoAoFecharFaturaInexistente() {
        // Arrange
        when(faturaRepository.findById(faturaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> faturaService.fecharFatura(faturaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Fatura não encontrada");

        verify(faturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve pagar fatura com sucesso quando estiver FECHADA")
    void devePagarFaturaComSucesso() {
        // Arrange
        Fatura fatura = Fatura.builder()
                .id(faturaId)
                .status(StatusFatura.FECHADA)
                .build();
        when(faturaRepository.findById(faturaId)).thenReturn(Optional.of(fatura));

        // Act
        faturaService.pagarFatura(faturaId);

        // Assert
        verify(faturaRepository, times(1)).save(faturaCaptor.capture());
        assertThat(faturaCaptor.getValue().getStatus()).isEqualTo(StatusFatura.PAGA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pagar fatura que não está FECHADA")
    void deveLancarExcecaoAoPagarFaturaNaoFechada() {
        // Arrange
        Fatura fatura = Fatura.builder()
                .id(faturaId)
                .status(StatusFatura.ABERTA) // Status inválido para pagamento
                .build();
        when(faturaRepository.findById(faturaId)).thenReturn(Optional.of(fatura));

        // Act & Assert
        assertThatThrownBy(() -> faturaService.pagarFatura(faturaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Apenas faturas FECHADAS podem ser pagas");

        verify(faturaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar pagar fatura inexistente")
    void deveLancarExcecaoAoPagarFaturaInexistente() {
        // Arrange
        when(faturaRepository.findById(faturaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> faturaService.pagarFatura(faturaId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Fatura não encontrada");

        verify(faturaRepository, never()).save(any());
    }
}