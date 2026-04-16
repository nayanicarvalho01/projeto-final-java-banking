package com.banking.transacao.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do SaldoClient")
class SaldoClientTest {

    private static WireMockServer wireMockServer;
    private SaldoClient saldoClient;
    private RestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        // ✅ Usa porta dinâmica (0 = porta aleatória disponível)
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void afterAll() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
        configureFor("localhost", wireMockServer.port()); // ✅ Usa porta dinâmica

        restTemplate = new RestTemplate();
        saldoClient = new SaldoClient(restTemplate);

        // ✅ Injeta URL com porta dinâmica
        ReflectionTestUtils.setField(
                saldoClient,
                "saldoServiceUrl",
                "http://localhost:" + wireMockServer.port()
        );
    }

    @Test
    @DisplayName("Deve atualizar saldo com débito com sucesso")
    void deveAtualizarSaldoDebitoComSucesso() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")));

        // Act & Assert
        assertThatCode(() -> saldoClient.atualizarSaldo(
                "12345",
                BigDecimal.valueOf(100.00),
                "DEBITO"
        )).doesNotThrowAnyException();

        verify(putRequestedFor(urlEqualTo("/api/saldos/12345"))
                .withRequestBody(containing("valor"))
                .withRequestBody(containing("tipo")));
    }

    @Test
    @DisplayName("Deve atualizar saldo com crédito com sucesso")
    void deveAtualizarSaldoCreditoComSucesso() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/67890"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        saldoClient.atualizarSaldo("67890", BigDecimal.valueOf(200.00), "CREDITO");

        // Assert
        verify(putRequestedFor(urlEqualTo("/api/saldos/67890"))
                .withRequestBody(containing("CREDITO"))
                .withRequestBody(containing("200")));
    }

    @Test
    @DisplayName("Deve lançar exceção quando saldo insuficiente (400)")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/12345"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Saldo insuficiente")));

        // Act & Assert
        assertThatThrownBy(() -> saldoClient.atualizarSaldo(
                "12345",
                BigDecimal.valueOf(1000.00),
                "DEBITO"
        )).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando serviço indisponível (500)")
    void deveLancarExcecaoQuandoServicoIndisponivel() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/12345"))
                .willReturn(aResponse().withStatus(500)));

        // Act & Assert
        assertThatThrownBy(() -> saldoClient.atualizarSaldo(
                "12345",
                BigDecimal.valueOf(100.00),
                "DEBITO"
        )).isInstanceOf(HttpServerErrorException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não encontrada (404)")
    void deveLancarExcecaoQuandoContaNaoEncontrada() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/99999"))
                .willReturn(aResponse().withStatus(404)));

        // Act & Assert
        assertThatThrownBy(() -> saldoClient.atualizarSaldo(
                "99999",
                BigDecimal.valueOf(100.00),
                "DEBITO"
        )).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    @DisplayName("Deve construir URL corretamente com contaId")
    void deveConstruirUrlCorretamenteComContaId() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/ABC123"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        saldoClient.atualizarSaldo("ABC123", BigDecimal.valueOf(100.00), "DEBITO");

        // Assert
        verify(putRequestedFor(urlEqualTo("/api/saldos/ABC123")));
    }

    @Test
    @DisplayName("Deve fazer PUT request (não POST)")
    void deveFazerPutRequest() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/12345"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        saldoClient.atualizarSaldo("12345", BigDecimal.valueOf(100.00), "DEBITO");

        // Assert
        verify(putRequestedFor(urlEqualTo("/api/saldos/12345")));
        verify(0, postRequestedFor(urlEqualTo("/api/saldos/12345")));
    }

    @Test
    @DisplayName("Deve processar valores decimais corretamente")
    void deveProcessarValoresDecimaisCorretamente() {
        // Arrange
        stubFor(put(urlEqualTo("/api/saldos/12345"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        saldoClient.atualizarSaldo("12345", new BigDecimal("99.99"), "DEBITO");

        // Assert
        verify(putRequestedFor(urlEqualTo("/api/saldos/12345"))
                .withRequestBody(containing("99.99")));
    }
}