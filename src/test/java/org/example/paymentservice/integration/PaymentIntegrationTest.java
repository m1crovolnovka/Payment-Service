package org.example.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
@AutoConfigureWireMock(port = 0)
class PaymentIntegrationTest {

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(
            WireMockConfiguration.wireMockConfig().dynamicPort()
    );

    @BeforeAll
    static void startWireMock() {
        WIRE_MOCK_SERVER.start();
    }

    @AfterAll
    static void stopWireMock() {
        WIRE_MOCK_SERVER.stop();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("random-service.url", () -> "http://localhost:" + WIRE_MOCK_SERVER.port());
        registry.add("spring.kafka.bootstrap-servers",
                () -> TestContainersConfig.kafka.getBootstrapServers());
    }

    @BeforeEach
    void cleanUp() {
        WIRE_MOCK_SERVER.resetAll();
        paymentRepository.deleteAll();
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        PaymentRequestDto request = new PaymentRequestDto(userId, orderId, new BigDecimal("100.00"));
        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathMatching("/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("2")));
        byte[] payload = objectMapper.writeValueAsBytes(request);
        kafkaTemplate.send("payment-requests", orderId.toString(), payload);
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    List<Payment> allPayments = paymentRepository.findAll();
                    Payment payment = allPayments.stream()
                            .findFirst()
                            .orElse(null);
                    assertThat(payment).as("Платеж с ID " + orderId + " не найден в БД").isNotNull();
                    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
                });
    }

    @Test
    void shouldSetFailedStatusOnServiceError() throws Exception {
        UUID orderId = UUID.randomUUID();
        PaymentRequestDto request = new PaymentRequestDto(UUID.randomUUID(), orderId, new BigDecimal("50.0"));
        WIRE_MOCK_SERVER.stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathMatching("/.*"))
                .willReturn(aResponse().withStatus(500)));
        kafkaTemplate.send("payment-requests", orderId.toString(), objectMapper.writeValueAsBytes(request));
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    Payment payment = paymentRepository.findAll().stream()
                            .findFirst()
                            .orElse(null);
                    assertThat(payment).isNotNull();
                    assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
                });
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldGetPaymentById() throws Exception {
        Payment saved = paymentRepository.save(createPayment(BigDecimal.valueOf(150.0)));
        mockMvc.perform(get("/api/payments/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentAmount").value(150.0));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldGetPaymentsByUserId() throws Exception {
        UUID userId = UUID.randomUUID();
        paymentRepository.save(createPaymentWithUser(userId, BigDecimal.valueOf(100.0)));
        paymentRepository.save(createPaymentWithUser(userId, BigDecimal.valueOf(200.0)));
        mockMvc.perform(get("/api/payments/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldGetPaymentsByOrderId() throws Exception {
        UUID orderId = UUID.randomUUID();
        paymentRepository.save(createPaymentWithOrder(orderId, BigDecimal.valueOf(100.0)));
        mockMvc.perform(get("/api/payments/order/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldGetPaymentsByStatus() throws Exception {
        paymentRepository.save(createPayment(BigDecimal.valueOf(10.0)));

        mockMvc.perform(get("/api/payments/status")
                        .param("status", "SUCCESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldCalculateUserTotalSum() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        paymentRepository.save(createPaymentWithUser(userId, new BigDecimal("100.50")));
        paymentRepository.save(createPaymentWithUser(userId, new BigDecimal("250.25")));
        mockMvc.perform(get("/api/payments/sum/user/{userId}", userId)
                        .param("startDate", now.minus(1, ChronoUnit.HOURS).toString())
                        .param("endDate", now.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("350.75"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldCalculateTotalSumForAll() throws Exception {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        paymentRepository.save(createPayment(new BigDecimal("500.00")));
        paymentRepository.save(createPayment(new BigDecimal("150.00")));
        mockMvc.perform(get("/api/payments/sum/total")
                        .param("startDate", now.minus(1, ChronoUnit.HOURS).toString())
                        .param("endDate", now.plus(1, ChronoUnit.HOURS).toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("650.00"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldReturnErrorWhenNotFound() throws Exception {
        mockMvc.perform(get("/api/payments/{id}", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError());
    }


    private Payment createPayment(BigDecimal amount) {
        return createPaymentWithUser(UUID.randomUUID(), amount);
    }

    private Payment createPaymentWithUser(UUID userId, BigDecimal amount) {
        return Payment.builder()
                .userId(userId)
                .orderId(UUID.randomUUID())
                .paymentAmount(amount)
                .status(PaymentStatus.SUCCESS)
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();
    }
    private Payment createPaymentWithOrder(UUID orderId, BigDecimal amount) {
        return Payment.builder()
                .userId(UUID.randomUUID())
                .orderId(orderId)
                .paymentAmount(amount)
                .status(PaymentStatus.SUCCESS)
                .timestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                .build();
    }
}