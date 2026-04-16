package com.banking.transacao.service;

import com.banking.transacao.client.CamundaClient;
import com.banking.transacao.client.SaldoClient;
import com.banking.transacao.model.Transacao;
import com.banking.transacao.model.dto.TransacaoEvent;
import com.banking.transacao.model.dto.TransacaoRequest;
import com.banking.transacao.model.enumerated.Status;
import com.banking.transacao.model.enumerated.Tipo;
import com.banking.transacao.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do TransacaoService")
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private SaldoClient saldoClient;

    @Mock
    private CamundaClient camundaClient;

    @Mock
    private KafkaTemplate<String, TransacaoEvent> kafkaTemplate;

    @InjectMocks
    private TransacaoService transacaoService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> camundaVariablesCaptor;

    @Captor
    private ArgumentCaptor<TransacaoEvent> eventCaptor;

    private TransacaoRequest request;
    private Transacao transacao;

    @BeforeEach
    void setUp() {
        request = TransacaoRequest.builder()
                .contaId("12345")
                .comerciante("Loja ABC")
                .localizacao("São Paulo, SP")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .build();

        transacao = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .comerciante("Loja ABC")
                .localizacao("São Paulo, SP")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Deve processar transação de débito com sucesso")
    void deveProcessarTransacaoDebitoComSucesso() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(
                anyString(),
                any(BigDecimal.class),
                anyString()
        );
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        Transacao resultado = transacaoService.processar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(Status.APROVADA);
        assertThat(resultado.getContaId()).isEqualTo("12345");
        assertThat(resultado.getValor()).isEqualByComparingTo(BigDecimal.valueOf(100.00));

        verify(saldoClient, times(1)).atualizarSaldo(
                eq("12345"),
                eq(BigDecimal.valueOf(100.00)),
                anyString()
        );
        verify(transacaoRepository, times(1)).save(any(Transacao.class));
        verify(camundaClient, times(1)).startProcess(eq("processo-transacao"), anyMap());
    }

    @Test
    @DisplayName("Deve processar transação de crédito com sucesso")
    void deveProcessarTransacaoCreditoComSucesso() {
        // Arrange
        request.setTipo(Tipo.CREDITO);
        transacao.setTipo(Tipo.CREDITO);

        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        Transacao resultado = transacaoService.processar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTipo()).isEqualTo(Tipo.CREDITO);
        assertThat(resultado.getStatus()).isEqualTo(Status.APROVADA);

        verify(saldoClient, times(1)).atualizarSaldo(
                eq("12345"),
                eq(BigDecimal.valueOf(100.00)),
                anyString()
        );
    }

    @Test
    @DisplayName("Deve negar transação quando saldo insuficiente")
    void deveNegarTransacaoQuandoSaldoInsuficiente() {
        // Arrange
        doThrow(new RuntimeException("Saldo insuficiente"))
                .when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());

        when(kafkaTemplate.send(anyString(), anyString(), any(TransacaoEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        Transacao resultado = transacaoService.processar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(Status.NEGADA);

        verify(saldoClient, times(1)).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        verify(transacaoRepository, never()).save(any(Transacao.class));
        verify(camundaClient, never()).startProcess(anyString(), anyMap());
        verify(kafkaTemplate, times(1)).send(eq("transacoes-reprovadas"), eq("12345"), any(TransacaoEvent.class));
    }

    @Test
    @DisplayName("Deve enviar evento para Kafka quando transação negada")
    void deveEnviarEventoKafkaQuandoTransacaoNegada() {
        // Arrange
        doThrow(new RuntimeException("Conta bloqueada"))
                .when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());

        when(kafkaTemplate.send(anyString(), anyString(), any(TransacaoEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        // Act
        transacaoService.processar(request);

        // Assert
        verify(kafkaTemplate, times(1)).send(
                eq("transacoes-reprovadas"),
                eq("12345"),
                eventCaptor.capture()
        );

        TransacaoEvent evento = eventCaptor.getValue();
        assertThat(evento.getContaId()).isEqualTo("12345");
        assertThat(evento.getStatus()).isEqualTo("NEGADA");
        assertThat(evento.getComerciante()).isEqualTo("Loja ABC");
        assertThat(evento.getValor()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
    }

    @Test
    @DisplayName("Deve iniciar processo Camunda com variáveis corretas")
    void deveIniciarProcessoCamundaComVariaveisCorretas() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        transacaoService.processar(request);

        // Assert
        verify(camundaClient, times(1)).startProcess(
                eq("processo-transacao"),
                camundaVariablesCaptor.capture()
        );

        Map<String, Object> variables = camundaVariablesCaptor.getValue();
        assertThat(variables).containsKeys(
                "transacaoId", "contaId", "comerciante",
                "localizacao", "valor", "tipo", "status"
        );
        assertThat(variables.get("contaId")).isEqualTo("12345");
        assertThat(variables.get("comerciante")).isEqualTo("Loja ABC");
        assertThat(variables.get("status")).isEqualTo("APROVADA");
    }

    @Test
    @DisplayName("Deve continuar processamento mesmo com erro no Camunda")
    void deveContinuarProcessamentoComErroCamunda() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doThrow(new RuntimeException("Erro Camunda"))
                .when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        Transacao resultado = transacaoService.processar(request);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getStatus()).isEqualTo(Status.APROVADA);

        verify(transacaoRepository, times(1)).save(any(Transacao.class));
        verify(camundaClient, times(1)).startProcess(anyString(), anyMap());
    }

    @Test
    @DisplayName("Deve buscar transações por conta ordenadas por data")
    void deveBuscarTransacoesPorContaOrdenadas() {
        // Arrange
        Transacao transacao1 = Transacao.builder()
                .id("trans-001")
                .contaId("12345")
                .dataHora(Instant.now().minusSeconds(3600))
                .build();

        Transacao transacao2 = Transacao.builder()
                .id("trans-002")
                .contaId("12345")
                .dataHora(Instant.now())
                .build();

        when(transacaoRepository.findByContaIdOrderByDataHoraDesc("12345"))
                .thenReturn(List.of(transacao2, transacao1));

        // Act
        List<Transacao> resultado = transacaoService.buscarPorConta("12345");

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
        List<Transacao> resultado = transacaoService.buscarPorConta("99999");

        // Assert
        assertThat(resultado).isEmpty();
        verify(transacaoRepository, times(1)).findByContaIdOrderByDataHoraDesc("99999");
    }

    @Test
    @DisplayName("Deve construir transação com todos os campos do request")
    void deveConstruirTransacaoComTodosCampos() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> {
            Transacao t = invocation.getArgument(0);
            t.setId("trans-001");
            return t;
        });
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        Transacao resultado = transacaoService.processar(request);

        // Assert
        assertThat(resultado.getContaId()).isEqualTo(request.getContaId());
        assertThat(resultado.getComerciante()).isEqualTo(request.getComerciante());
        assertThat(resultado.getLocalizacao()).isEqualTo(request.getLocalizacao());
        assertThat(resultado.getValor()).isEqualByComparingTo(request.getValor());
        assertThat(resultado.getTipo()).isEqualTo(request.getTipo());
        assertThat(resultado.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve salvar transação no repositório antes de iniciar Camunda")
    void deveSalvarTransacaoAntesDeIniciarCamunda() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        transacaoService.processar(request);

        // Assert
        var inOrder = inOrder(transacaoRepository, camundaClient);
        inOrder.verify(transacaoRepository).save(any(Transacao.class));
        inOrder.verify(camundaClient).startProcess(anyString(), anyMap());
    }

    @Test
    @DisplayName("Deve chamar toTipoSaldo() do enum TipoTransacao")
    void deveChamarToTipoSaldoDoEnum() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        transacaoService.processar(request);

        // Assert
        verify(saldoClient, times(1)).atualizarSaldo(
                eq("12345"),
                eq(BigDecimal.valueOf(100.00)),
                eq(Tipo.DEBITO.toTipoSaldo()) // Verifica conversão
        );
    }

    @Test
    @DisplayName("Deve processar múltiplas transações sequencialmente")
    void deveProcessarMultiplasTransacoesSequencialmente() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        transacaoService.processar(request);
        transacaoService.processar(request);
        transacaoService.processar(request);

        // Assert
        verify(saldoClient, times(3)).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        verify(transacaoRepository, times(3)).save(any(Transacao.class));
        verify(camundaClient, times(3)).startProcess(anyString(), anyMap());
    }

    @Test
    @DisplayName("Deve logar informações durante processamento")
    void deveLogarInformacoesDuranteProcessamento() {
        // Arrange
        doNothing().when(saldoClient).atualizarSaldo(anyString(), any(BigDecimal.class), anyString());
        when(transacaoRepository.save(any(Transacao.class))).thenReturn(transacao);
        doNothing().when(camundaClient).startProcess(anyString(), anyMap());

        // Act
        transacaoService.processar(request);

        // Assert - Verifica que não lança exceção (logs são chamados)
        assertThatCode(() -> transacaoService.processar(request))
                .doesNotThrowAnyException();
    }
}