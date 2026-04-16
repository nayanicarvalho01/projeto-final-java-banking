package com.banking.transacao.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("Testes do CamundaClient")
class CamundaClientTest {

    private static WireMockServer wireMockServer;
    private CamundaClient camundaClient;
    private RestTemplate restTemplate;

    @BeforeAll
    static void beforeAll() {
        // ✅ Usa porta dinâmica
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
        camundaClient = new CamundaClient(restTemplate);

        // ✅ Injeta URL com porta dinâmica
        ReflectionTestUtils.setField(
                camundaClient,
                "camundaUrl",
                "http://localhost:" + wireMockServer.port() + "/engine-rest"
        );
    }

    @Test
    @DisplayName("Deve iniciar processo Camunda com sucesso")
    void deveIniciarProcessoComSucesso() {
        // Arrange
        stubFor(post(urlEqualTo("/engine-rest/process-definition/key/processo-transacao/start"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"proc-001\"}")));

        Map<String, Object> variables = new HashMap<>();
        variables.put("transacaoId", "trans-001");
        variables.put("contaId", "12345");

        // Act & Assert
        assertThatCode(() -> camundaClient.startProcess("processo-transacao", variables))
                .doesNotThrowAnyException();

        verify(postRequestedFor(urlEqualTo("/engine-rest/process-definition/key/processo-transacao/start"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("Deve formatar variável String corretamente")
    void deveFormatarVariavelStringCorretamente() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("transacaoId", "trans-001");

        // Act
        camundaClient.startProcess("processo-transacao", variables);

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withRequestBody(containing("\"transacaoId\""))
                .withRequestBody(containing("\"value\":\"trans-001\""))
                .withRequestBody(containing("\"type\":\"String\"")));
    }

    @Test
    @DisplayName("Deve formatar variável Long corretamente")
    void deveFormatarVariavelLongCorretamente() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("quantidade", 10L);

        // Act
        camundaClient.startProcess("processo-transacao", variables);

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withRequestBody(containing("\"quantidade\""))
                .withRequestBody(containing("\"type\":\"Long\"")));
    }

    @Test
    @DisplayName("Deve formatar variável BigDecimal como Double")
    void deveFormatarVariavelBigDecimalComoDouble() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("valor", BigDecimal.valueOf(100.50));

        // Act
        camundaClient.startProcess("processo-transacao", variables);

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withRequestBody(containing("\"valor\""))
                .withRequestBody(containing("\"type\":\"Double\"")));
    }

    @Test
    @DisplayName("Deve formatar variável Boolean corretamente")
    void deveFormatarVariavelBooleanCorretamente() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("aprovado", true);

        // Act
        camundaClient.startProcess("processo-transacao", variables);

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withRequestBody(containing("\"aprovado\""))
                .withRequestBody(containing("\"value\":true"))
                .withRequestBody(containing("\"type\":\"Boolean\"")));
    }

    @Test
    @DisplayName("Deve lançar RuntimeException quando Camunda retorna erro 500")
    void deveLancarExcecaoQuandoCamundaRetornaErro500() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(500)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("transacaoId", "trans-001");

        // Act & Assert
        assertThatThrownBy(() -> camundaClient.startProcess("processo-transacao", variables))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Falha ao iniciar processo Camunda");
    }

    @Test
    @DisplayName("Deve enviar Content-Type application/json")
    void deveEnviarContentTypeJson() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        camundaClient.startProcess("processo-transacao", new HashMap<>());

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withHeader("Content-Type", equalTo("application/json")));
    }

    @Test
    @DisplayName("Deve processar múltiplas variáveis com tipos diferentes")
    void deveProcessarMultiplasVariaveisComTiposDiferentes() {
        // Arrange
        stubFor(post(urlMatching("/engine-rest/.*"))
                .willReturn(aResponse().withStatus(200)));

        Map<String, Object> variables = new HashMap<>();
        variables.put("transacaoId", "trans-001");
        variables.put("valor", BigDecimal.valueOf(100));
        variables.put("quantidade", 5L);
        variables.put("aprovado", true);

        // Act
        camundaClient.startProcess("processo-transacao", variables);

        // Assert
        verify(postRequestedFor(urlMatching("/engine-rest/.*"))
                .withRequestBody(containing("\"transacaoId\""))
                .withRequestBody(containing("\"type\":\"String\""))
                .withRequestBody(containing("\"valor\""))
                .withRequestBody(containing("\"type\":\"Double\"")));
    }
}