package com.banking.notificacao.kafka;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
import com.banking.notificacao.model.NotificacaoEvent;
import com.banking.notificacao.service.NotificacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificacaoConsumer")
class NotificacaoConsumerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private NotificacaoConsumer notificacaoConsumer;

    @Captor
    private ArgumentCaptor<NotificacaoEvent> eventCaptor;

    private ObjectMapper objectMapper;
    private String mensagemAprovada;
    private String mensagemNegada;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Injeta ObjectMapper real no consumer
        notificacaoConsumer = new NotificacaoConsumer(notificacaoService, objectMapper);

        mensagemAprovada = """
                {
                    "transacaoId": "trans-001",
                    "contaId": "12345",
                    "comerciante": "Loja ABC",
                    "localizacao": "São Paulo, SP",
                    "valor": 100.00,
                    "tipo": "DEBITO",
                    "status": "APROVADA",
                    "dataHora": 1704067200
                }
                """;

        mensagemNegada = """
                {
                    "transacaoId": "trans-002",
                    "contaId": "67890",
                    "comerciante": "Loja XYZ",
                    "localizacao": "Rio de Janeiro, RJ",
                    "valor": 500.00,
                    "tipo": "CREDITO",
                    "status": "NEGADA",
                    "dataHora": 1704067200
                }
                """;
    }

    @Test
    @DisplayName("Deve processar mensagem aprovada com sucesso")
    void deveProcessarMensagemAprovadaComSucesso() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());

        NotificacaoEvent evento = eventCaptor.getValue();
        assertThat(evento.getTransacaoId()).isEqualTo("trans-001");
        assertThat(evento.getContaId()).isEqualTo("12345");
        assertThat(evento.getComerciante()).isEqualTo("Loja ABC");
        assertThat(evento.getLocalizacao()).isEqualTo("São Paulo, SP");
        assertThat(evento.getValor()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(evento.getTipo()).isEqualTo(Tipo.DEBITO);
        assertThat(evento.getStatus()).isEqualTo(Status.APROVADA);
    }

    @Test
    @DisplayName("Deve processar mensagem negada com sucesso")
    void deveProcessarMensagemNegadaComSucesso() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemNegada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());

        NotificacaoEvent evento = eventCaptor.getValue();
        assertThat(evento.getTransacaoId()).isEqualTo("trans-002");
        assertThat(evento.getContaId()).isEqualTo("67890");
        assertThat(evento.getTipo()).isEqualTo(Tipo.CREDITO);
        assertThat(evento.getStatus()).isEqualTo(Status.NEGADA);
    }

    @Test
    @DisplayName("Deve deserializar JSON corretamente")
    void deveDeserializarJsonCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());

        NotificacaoEvent evento = eventCaptor.getValue();
        assertThat(evento).isNotNull();
        assertThat(evento.getTransacaoId()).isNotNull();
        assertThat(evento.getContaId()).isNotNull();
        assertThat(evento.getValor()).isNotNull();
        assertThat(evento.getTipo()).isNotNull();
        assertThat(evento.getStatus()).isNotNull();
        assertThat(evento.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve converter valor BigDecimal corretamente")
    void deveConverterValorBigDecimalCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        String mensagemDecimal = """
                {
                    "transacaoId": "trans-003",
                    "contaId": "12345",
                    "comerciante": "Loja",
                    "localizacao": "SP",
                    "valor": 99.99,
                    "tipo": "DEBITO",
                    "status": "APROVADA",
                    "dataHora": 1704067200
                }
                """;

        // Act
        consumer.accept(mensagemDecimal);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getValor()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Deve converter timestamp para Instant corretamente")
    void deveConverterTimestampParaInstantCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve processar tipo DEBITO corretamente")
    void deveProcessarTipoDebitoCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getTipo()).isEqualTo(Tipo.DEBITO);
    }

    @Test
    @DisplayName("Deve processar tipo CREDITO corretamente")
    void deveProcessarTipoCreditoCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemNegada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getTipo()).isEqualTo(Tipo.CREDITO);
    }

    @Test
    @DisplayName("Deve processar status APROVADA corretamente")
    void deveProcessarStatusAprovadaCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(Status.APROVADA);
    }

    @Test
    @DisplayName("Deve processar status NEGADA corretamente")
    void deveProcessarStatusNegadaCorretamente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemNegada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getStatus()).isEqualTo(Status.NEGADA);
    }

    @Test
    @DisplayName("Deve tratar erro de JSON inválido sem lançar exceção")
    void deveTratarErroDeJsonInvalido() {
        // Arrange
        Consumer<String> consumer = notificacaoConsumer.notificacoes();
        String jsonInvalido = "{ invalid json }";

        // Act & Assert
        assertThatCode(() -> consumer.accept(jsonInvalido))
                .doesNotThrowAnyException();

        verify(notificacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("Deve tratar erro quando campo obrigatório está ausente")
    void deveTratarErroCampoObrigatorioAusente() {
        // Arrange
        Consumer<String> consumer = notificacaoConsumer.notificacoes();
        String mensagemIncompleta = """
                {
                    "transacaoId": "trans-001",
                    "contaId": "12345"
                }
                """;

        // Act & Assert
        assertThatCode(() -> consumer.accept(mensagemIncompleta))
                .doesNotThrowAnyException();

        verify(notificacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("Deve tratar erro quando enum inválido")
    void deveTratarErroEnumInvalido() {
        // Arrange
        Consumer<String> consumer = notificacaoConsumer.notificacoes();
        String mensagemEnumInvalido = """
                {
                    "transacaoId": "trans-001",
                    "contaId": "12345",
                    "comerciante": "Loja",
                    "localizacao": "SP",
                    "valor": 100.00,
                    "tipo": "INVALIDO",
                    "status": "APROVADA",
                    "dataHora": 1704067200
                }
                """;

        // Act & Assert
        assertThatCode(() -> consumer.accept(mensagemEnumInvalido))
                .doesNotThrowAnyException();

        verify(notificacaoService, never()).processar(any());
    }

    @Test
    @DisplayName("Deve retornar Consumer válido")
    void deveRetornarConsumerValido() {
        // Act
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Assert
        assertThat(consumer).isNotNull();
    }

    @Test
    @DisplayName("Deve processar múltiplas mensagens sequencialmente")
    void deveProcessarMultiplasMensagensSequencialmente() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);
        consumer.accept(mensagemNegada);
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(3)).processar(any(NotificacaoEvent.class));
    }

    @Test
    @DisplayName("Deve preservar todos os campos da mensagem")
    void devePreservarTodosCamposDaMensagem() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(eventCaptor.capture());

        NotificacaoEvent evento = eventCaptor.getValue();
        assertThat(evento.getTransacaoId()).isNotBlank();
        assertThat(evento.getContaId()).isNotBlank();
        assertThat(evento.getComerciante()).isNotBlank();
        assertThat(evento.getLocalizacao()).isNotBlank();
        assertThat(evento.getValor()).isPositive();
        assertThat(evento.getTipo()).isNotNull();
        assertThat(evento.getStatus()).isNotNull();
        assertThat(evento.getDataHora()).isNotNull();
    }

    @Test
    @DisplayName("Deve chamar service apenas uma vez por mensagem")
    void deveChamarServiceApenasUmaVezPorMensagem() {
        // Arrange
        when(notificacaoService.processar(any(NotificacaoEvent.class))).thenReturn(true);
        Consumer<String> consumer = notificacaoConsumer.notificacoes();

        // Act
        consumer.accept(mensagemAprovada);

        // Assert
        verify(notificacaoService, times(1)).processar(any(NotificacaoEvent.class));
    }
}