package com.banking.notificacao.controller;

import com.banking.notificacao.service.NotificacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do NotificacaoController")
class NotificacaoControllerTest {

    @Mock
    private NotificacaoService notificacaoService;

    @InjectMocks
    private NotificacaoController notificacaoController;

    private SseEmitter sseEmitter;

    @BeforeEach
    void setUp() {
        sseEmitter = new SseEmitter(3600000L);
    }

    @Test
    @DisplayName("Deve iniciar stream SSE com sucesso")
    void deveIniciarStreamSSEComSucesso() {
        // Arrange
        when(notificacaoService.registrarCliente("12345")).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream("12345");

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado).isEqualTo(sseEmitter);
        verify(notificacaoService, times(1)).registrarCliente("12345");
    }

    @Test
    @DisplayName("Deve aceitar contaId numérico")
    void deveAceitarContaIdNumerico() {
        // Arrange
        when(notificacaoService.registrarCliente("67890")).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream("67890");

        // Assert
        assertThat(resultado).isNotNull();
        verify(notificacaoService, times(1)).registrarCliente("67890");
    }

    @Test
    @DisplayName("Deve aceitar contaId alfanumérico")
    void deveAceitarContaIdAlfanumerico() {
        // Arrange
        when(notificacaoService.registrarCliente("ABC123")).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream("ABC123");

        // Assert
        assertThat(resultado).isNotNull();
        verify(notificacaoService, times(1)).registrarCliente("ABC123");
    }

    @Test
    @DisplayName("Deve aceitar contaId com caracteres especiais")
    void deveAceitarContaIdComCaracteresEspeciais() {
        // Arrange
        when(notificacaoService.registrarCliente("ABC-123_XYZ")).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream("ABC-123_XYZ");

        // Assert
        assertThat(resultado).isNotNull();
        verify(notificacaoService, times(1)).registrarCliente("ABC-123_XYZ");
    }

    @Test
    @DisplayName("Deve retornar SseEmitter do service")
    void deveRetornarSseEmitterDoService() {
        // Arrange
        SseEmitter emitterEsperado = new SseEmitter(3600000L);
        when(notificacaoService.registrarCliente("12345")).thenReturn(emitterEsperado);

        // Act
        SseEmitter resultado = notificacaoController.stream("12345");

        // Assert
        assertThat(resultado).isEqualTo(emitterEsperado);
    }

    @Test
    @DisplayName("Deve permitir múltiplas conexões")
    void devePermitirMultiplasConexoes() {
        // Arrange
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        when(notificacaoService.registrarCliente("12345")).thenReturn(emitter1);
        when(notificacaoService.registrarCliente("67890")).thenReturn(emitter2);

        // Act
        SseEmitter resultado1 = notificacaoController.stream("12345");
        SseEmitter resultado2 = notificacaoController.stream("67890");

        // Assert
        assertThat(resultado1).isEqualTo(emitter1);
        assertThat(resultado2).isEqualTo(emitter2);
        verify(notificacaoService, times(1)).registrarCliente("12345");
        verify(notificacaoService, times(1)).registrarCliente("67890");
    }

    @Test
    @DisplayName("Deve permitir reconexão do mesmo cliente")
    void devePermitirReconexaoDoMesmoCliente() {
        // Arrange
        SseEmitter emitter1 = new SseEmitter();
        SseEmitter emitter2 = new SseEmitter();

        when(notificacaoService.registrarCliente("12345"))
                .thenReturn(emitter1)
                .thenReturn(emitter2);

        // Act
        SseEmitter resultado1 = notificacaoController.stream("12345");
        SseEmitter resultado2 = notificacaoController.stream("12345");

        // Assert
        assertThat(resultado1).isEqualTo(emitter1);
        assertThat(resultado2).isEqualTo(emitter2);
        verify(notificacaoService, times(2)).registrarCliente("12345");
    }

    @Test
    @DisplayName("Deve funcionar com contaId longo")
    void deveFuncionarComContaIdLongo() {
        // Arrange
        String contaIdLongo = "1234567890123456789012345678901234567890";
        when(notificacaoService.registrarCliente(contaIdLongo)).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream(contaIdLongo);

        // Assert
        assertThat(resultado).isNotNull();
        verify(notificacaoService, times(1)).registrarCliente(contaIdLongo);
    }

    @Test
    @DisplayName("Deve delegar para o service")
    void deveDelegarParaService() {
        // Arrange
        when(notificacaoService.registrarCliente(anyString())).thenReturn(sseEmitter);

        // Act
        notificacaoController.stream("12345");

        // Assert
        verify(notificacaoService, times(1)).registrarCliente("12345");
    }

    @Test
    @DisplayName("Deve retornar emitter não nulo")
    void deveRetornarEmitterNaoNulo() {
        // Arrange
        when(notificacaoService.registrarCliente(anyString())).thenReturn(sseEmitter);

        // Act
        SseEmitter resultado = notificacaoController.stream("12345");

        // Assert
        assertThat(resultado).isNotNull();
    }

    @Test
    @DisplayName("Deve processar múltiplas requisições sequencialmente")
    void deveProcessarMultiplasRequisicoesSequencialmente() {
        // Arrange
        when(notificacaoService.registrarCliente(anyString())).thenReturn(sseEmitter);

        // Act
        notificacaoController.stream("12345");
        notificacaoController.stream("12345");
        notificacaoController.stream("12345");

        // Assert
        verify(notificacaoService, times(3)).registrarCliente("12345");
    }

    @Test
    @DisplayName("Deve chamar service com parâmetro correto")
    void deveChamarServiceComParametroCorreto() {
        // Arrange
        when(notificacaoService.registrarCliente("TESTE-123")).thenReturn(sseEmitter);

        // Act
        notificacaoController.stream("TESTE-123");

        // Assert
        verify(notificacaoService, times(1)).registrarCliente("TESTE-123");
        verify(notificacaoService, never()).registrarCliente("OUTRO-ID");
    }
}