package org.example.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.security.JwtFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestContainersConfig.class) // Ключевое: подключаем наш конфиг с заглушкой
public class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "USER")
    @DisplayName("Успешное получение платежа по ID")
    void shouldGetPaymentById() throws Exception {
        // Arrange

        // Act & Assert
        mockMvc.perform(get("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // ВНИМАНИЕ: проверь, что в JSON поле называется 'id', а не '_id'
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                // Проверь имя поля: paymentAmount или amount?
                .andExpect(jsonPath("$.paymentAmount").value(10.0));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @DisplayName("Доступ запрещен для USER на админский эндпоинт")
    void shouldReturn403WhenUserTriesAdminEndpoint() throws Exception {
        // Если мы заходим под USER на эндпоинт, требующий ADMIN
        mockMvc.perform(get("/api/payments/sum/total"))
                .andExpect(status().isForbidden());
    }
}
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ActiveProfiles("test") // Обязательно!
//@Import(TestContainersConfig.class)
//public class PaymentIntegrationTest {
//
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Autowired
//    private KafkaTemplate<String, byte[]> kafkaTemplate;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @DynamicPropertySource
//    static void overrideProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.mongodb.uri", TestContainersConfig.MONGO_CONTAINER::getReplicaSetUrl);
//        registry.add("spring.kafka.bootstrap-servers", TestContainersConfig.KAFKA_CONTAINER::getBootstrapServers);
//    }
//
//    @BeforeEach
//    void setUp() {
//        paymentRepository.deleteAll(); // Чистим базу перед каждым тестом
//    }
//
//    private PaymentResponseDto payment() {
//        return new PaymentResponseDto(
//                UUID.randomUUID().toString(),
//                UUID.randomUUID(),
//                UUID.randomUUID(),
//                PaymentStatus.SUCCESS,
//                BigDecimal.TEN,
//                Instant.now()
//        );
//    }
//
//
//    @Test
//    @WithMockUser(authorities = "USER")
//    void shouldGetPaymentById() throws Exception {
//
//        Payment saved = paymentRepository.save(
//                Payment.builder()
//                        .orderId(UUID.randomUUID())
//                        .userId(UUID.randomUUID())
//                        .paymentAmount(BigDecimal.TEN)
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                        .build()
//        );
//
//        mockMvc.perform(get("/api/payments/{id}", saved.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(saved.getId()))
//                .andExpect(jsonPath("$.status").value("SUCCESS"));
//    }
//
//    // ---------- BY USER ----------
//
//    @Test
//    @WithMockUser(authorities = "USER")
//    void shouldGetPaymentsByUserId() throws Exception {
//        UUID userId = UUID.randomUUID();
//        Payment p1 = Payment.builder()
//                .orderId(UUID.randomUUID())
//                .userId(userId)
//                .paymentAmount(BigDecimal.TEN)
//                .status(PaymentStatus.SUCCESS)
//                .timestamp(Instant.now())
//                .build();
//        Payment p2 = Payment.builder()
//                .orderId(UUID.randomUUID())
//                .userId(userId)
//                .paymentAmount(BigDecimal.ONE)
//                .status(PaymentStatus.FAILED)
//                .timestamp(Instant.now())
//                .build();
//
//        paymentRepository.saveAll(List.of(p1,p2));
//
//        mockMvc.perform(get("/api/payments/user/{userId}", userId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2));
//    }
//
//    // ---------- BY ORDER ----------
//
//    @Test
//    @WithMockUser(authorities = "USER")
//    void shouldGetPaymentsByOrderId() throws Exception {
//        UUID orderId = UUID.randomUUID();
//
//        paymentRepository.save(
//                Payment.builder()
//                        .orderId(orderId)
//                        .userId(UUID.randomUUID())
//                        .paymentAmount(BigDecimal.TEN)
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                        .build()
//        );
//
//        mockMvc.perform(get("/api/payments/order/{orderId}", orderId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(1));
//    }
//
//    // ---------- BY STATUS ----------
//
//    @Test
//    @WithMockUser(authorities = "ADMIN")
//    void shouldGetPaymentsByStatus() throws Exception {
//        paymentRepository.save(
//                Payment.builder()
//                        .orderId(UUID.randomUUID())
//                        .userId(UUID.randomUUID())
//                        .paymentAmount(BigDecimal.TEN)
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                        .build()
//        );
//
//        mockMvc.perform(get("/api/payments/status")
//                        .param("status", "SUCCESS"))
//                .andExpect(status().isOk())
//                .andExpect(result -> {
//                    String json = result.getResponse().getContentAsString();
//                    assertThat(json).isNotBlank();
//                });
//        ;
//    }
//
//    // ---------- SUM BY USER ----------
//
//    @Test
//    @WithMockUser(authorities = "USER")
//    void shouldGetTotalSumForUser() throws Exception {
//        UUID userId = UUID.randomUUID();
//
//        paymentRepository.save(
//                Payment.builder()
//                        .orderId(UUID.randomUUID())
//                        .userId(userId)
//                        .paymentAmount(BigDecimal.TEN)
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                        .build()
//        );
//
//        mockMvc.perform(get("/api/payments/sum/user/{userId}", userId)
//                        .param("startDate", Instant.now().minusSeconds(3600).toString())
//                        .param("endDate", Instant.now().plusSeconds(3600).toString()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("10.0"));
//    }
//
//    // ---------- SUM TOTAL ----------
//
//    @Test
//    @WithMockUser(authorities = "ADMIN")
//    void shouldGetTotalSumForAll() throws Exception {
//        paymentRepository.save(
//                Payment.builder()
//                        .orderId(UUID.randomUUID())
//                        .userId(UUID.randomUUID())
//                        .paymentAmount(BigDecimal.TEN)
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                        .build()
//        );
//
//        mockMvc.perform(get("/api/payments/sum/total")
//                        .param("startDate", Instant.now().minusSeconds(3600).toString())
//                        .param("endDate", Instant.now().plusSeconds(3600).toString()))
//                .andExpect(status().isOk())
//                .andExpect(content().string("10"));
//    }

    // --- ТЕСТЫ KAFKA ---

//    @Test
//    @DisplayName("Kafka: создание платежа при получении сообщения")
//    void shouldCreatePaymentWhenMessageReceived() throws Exception {
//        // Arrange
//        String orderId = UUID.randomUUID().toString();
//        PaymentRequestDto requestDto = new PaymentRequestDto(UUID.fromString(orderId), UUID.randomUUID(), BigDecimal.TEN);
//
//        byte[] data = objectMapper.writeValueAsBytes(requestDto);
//        // Act
//        kafkaTemplate.send("payment-requests", orderId, data);
//
//        // Assert
//        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
//            var saved = paymentRepository.findAll().stream()
//                    .filter(p -> p.getOrderId().equals(orderId))
//                    .findFirst();
//            assertThat(saved).isPresent();
//            assertThat(saved.get().getStatus()).isEqualTo(PaymentStatus.FAILED); // Или твой статус по умолчанию
//        });
//    }

    // --- ТЕСТЫ КОНТРОЛЛЕРА ---

//    @Test
//    @WithMockUser(authorities = {"ADMIN"})
//    void shouldGetAllPayments() throws Exception {
//        // Arrange
//        paymentRepository.save(Payment.builder()
//                        .id(null)
//                        .paymentAmount(BigDecimal.TEN)
//                        .orderId(UUID.randomUUID())
//                        .userId(UUID.randomUUID())
//                        .status(PaymentStatus.SUCCESS)
//                        .timestamp(Instant.now())
//                .build());
//        paymentRepository.save(Payment.builder()
//                .id(null)
//                .paymentAmount(BigDecimal.ONE)
//                .orderId(UUID.randomUUID())
//                .userId(UUID.randomUUID())
//                .status(PaymentStatus.FAILED)
//                .timestamp(Instant.now())
//                .build());
//
//        // Act & Assert
//        mockMvc.perform(get("/api/payments"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2))
//                .andExpect(jsonPath("$[0].orderId").exists());
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ADMIN"})
//    void shouldGetPaymentById() throws Exception {
//        // Arrange
//        Payment saved = paymentRepository.save(Payment.builder()
//                .id(null)
//                .paymentAmount(BigDecimal.TEN)
//                .orderId(UUID.randomUUID())
//                .userId(UUID.randomUUID())
//                .status(PaymentStatus.SUCCESS)
//                .timestamp(Instant.now())
//                .build());;
//
//        // Act & Assert
//        mockMvc.perform(get("/api/payments/" + saved.getId()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value(saved.getId()))
//                .andExpect(jsonPath("$.orderId").value(saved.getOrderId()))
//                .andExpect(jsonPath("$.amount").value(BigDecimal.TEN));
//    }
//
//    @Test
//    @WithMockUser(authorities = {"ADMIN"})
//    void shouldReturn404WhenNotFound() throws Exception {
//        mockMvc.perform(get("/api/payments/" + UUID.randomUUID()))
//                .andExpect(status().isNotFound());
//    }
