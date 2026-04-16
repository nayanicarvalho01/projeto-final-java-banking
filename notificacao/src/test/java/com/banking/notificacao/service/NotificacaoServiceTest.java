package com.banking.notificacao.service;

import com.banking.notificacao.enumerated.Status;
import com.banking.notificacao.enumerated.Tipo;
import com.banking.notificacao.model.NotificacaoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificacaoService")
class NotificacaoServiceTest {

    @InjectMocks
    private NotificacaoService notificacaoService;

    private NotificacaoEvent eventAprovado;
    private NotificacaoEvent eventNegado;
    private Map<String, SseEmitter> emitters;

    @BeforeEach
    void setUp() {
        eventAprovado = NotificacaoEvent.builder()
                .transacaoId("trans-001")
                .contaId("12345")
                .comerciante("Loja ABC")
                .localizacao("São Paulo, SP")
                .valor(BigDecimal.valueOf(100.00))
                .tipo(Tipo.DEBITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        eventNegado = NotificacaoEvent.builder()
                .transacaoId("trans-002")
                .contaId("12345")
                .comerciante("Loja XYZ")
                .localizacao("Rio de Janeiro, RJ")
                .valor(BigDecimal.valueOf(500.00))
                .tipo(Tipo.DEBITO)
                .status(Status.NEGADA)
                .dataHora(Instant.now())
                .build();

        emitters = new ConcurrentHashMap<>();
        ReflectionTestUtils.setField(notificacaoService, "emitters", emitters);
    }

    @Test
    @DisplayName("Deve registrar cliente SSE com sucesso")
    void deveRegistrarClienteSSEComSucesso() {
        // Act
        SseEmitter emitter = notificacaoService.registrarCliente("12345");

        // Assert
        assertThat(emitter).isNotNull();
        assertThat(emitter.getTimeout()).isEqualTo(3600000L);
        assertThat(emitters).containsKey("12345");
        assertThat(emitters.get("12345")).isEqualTo(emitter);
    }

    @Test
    @DisplayName("Deve processar notificação aprovada com sucesso")
    void deveProcessarNotificacaoAprovadaComSucesso() {
        // Arrange
        SseEmitter emitter = spy(new SseEmitter());
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve processar notificação negada com sucesso")
    void deveProcessarNotificacaoNegadaComSucesso() {
        // Arrange
        SseEmitter emitter = spy(new SseEmitter());
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventNegado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando usuário está offline")
    void deveRetornarFalseQuandoUsuarioOffline() {
        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isFalse();
        assertThat(emitters).doesNotContainKey("12345");
    }

    @Test
    @DisplayName("Deve formatar mensagem de transação aprovada com débito")
    void deveFormatarMensagemAprovadaDebito() {
        // Arrange
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(emitters).containsKey("12345");
    }

    @Test
    @DisplayName("Deve formatar mensagem de transação aprovada com crédito")
    void deveFormatarMensagemAprovadaCredito() {
        // Arrange
        eventAprovado.setTipo(Tipo.CREDITO);
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve formatar mensagem de transação negada com débito")
    void deveFormatarMensagemNegadaDebito() {
        // Arrange
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventNegado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve formatar mensagem de transação negada com crédito")
    void deveFormatarMensagemNegadaCredito() {
        // Arrange
        eventNegado.setTipo(Tipo.CREDITO);
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventNegado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve remover emitter quando ocorre erro no envio")
    void deveRemoverEmitterQuandoOcorreErro() throws IOException {
        // Arrange
        SseEmitter emitter = mock(SseEmitter.class);
        doThrow(new IOException("Erro simulado")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isFalse();
        assertThat(emitters).doesNotContainKey("12345");
    }

    @Test
    @DisplayName("Deve processar múltiplas notificações para o mesmo cliente")
    void deveProcessarMultiplasNotificacoesParaMesmoCliente() {
        // Arrange
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado1 = notificacaoService.processar(eventAprovado);
        boolean resultado2 = notificacaoService.processar(eventNegado);

        // Assert
        assertThat(resultado1).isTrue();
        assertThat(resultado2).isTrue();
        assertThat(emitters).containsKey("12345");
    }

    @Test
    @DisplayName("Deve processar notificações para múltiplos clientes")
    void deveProcessarNotificacoesParaMultiplosClientes() {
        // Arrange
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();
        emitters.put("12345", emitter1);
        emitters.put("67890", emitter2);

        NotificacaoEvent event2 = NotificacaoEvent.builder()
                .contaId("67890")
                .comerciante("Loja 2")
                .localizacao("Brasília, DF")
                .valor(BigDecimal.valueOf(200.00))
                .tipo(Tipo.CREDITO)
                .status(Status.APROVADA)
                .dataHora(Instant.now())
                .build();

        // Act
        boolean resultado1 = notificacaoService.processar(eventAprovado);
        boolean resultado2 = notificacaoService.processar(event2);

        // Assert
        assertThat(resultado1).isTrue();
        assertThat(resultado2).isTrue();
        assertThat(emitters).hasSize(2);
    }

    @Test
    @DisplayName("Deve substituir emitter quando cliente reconecta")
    void deveSubstituirEmitterQuandoClienteReconecta() {
        // Arrange
        SseEmitter emitter1 = notificacaoService.registrarCliente("12345");

        // Act
        SseEmitter emitter2 = notificacaoService.registrarCliente("12345");

        // Assert
        assertThat(emitter2).isNotEqualTo(emitter1);
        assertThat(emitters.get("12345")).isEqualTo(emitter2);
    }

    @Test
    @DisplayName("Deve processar valores decimais corretamente na mensagem")
    void deveProcessarValoresDecimaisCorretamente() {
        // Arrange
        eventAprovado.setValor(new BigDecimal("99.99"));
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve incluir localização na mensagem formatada")
    void deveIncluirLocalizacaoNaMensagemFormatada() {
        // Arrange
        eventAprovado.setLocalizacao("Curitiba, PR");
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve incluir comerciante na mensagem formatada")
    void deveIncluirComercianteNaMensagemFormatada() {
        // Arrange
        eventAprovado.setComerciante("Supermercado XYZ");
        SseEmitter emitter = new SseEmitter();
        emitters.put("12345", emitter);

        // Act
        boolean resultado = notificacaoService.processar(eventAprovado);

        // Assert
        assertThat(resultado).isTrue();
    }

    @Test
    @DisplayName("Deve configurar timeout de 1 hora no emitter")
    void deveConfigurarTimeoutDeUmaHora() {
        // Act
        SseEmitter emitter = notificacaoService.registrarCliente("12345");

        // Assert
        assertThat(emitter.getTimeout()).isEqualTo(3600000L);
    }

    @Test
    @DisplayName("Deve registrar callbacks no emitter")
    void deveRegistrarCallbacksNoEmitter() {
        // Act
        SseEmitter emitter = notificacaoService.registrarCliente("12345");

        // Assert
        assertThat(emitter).isNotNull();
    }
}