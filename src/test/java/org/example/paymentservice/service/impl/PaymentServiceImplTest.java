package org.example.paymentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.client.RandomServiceClient;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.mapper.PaymentMapper;
import org.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RandomServiceClient randomServiceClient;
    @Mock
    private KafkaTemplate<String, byte[]> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;
    private UUID orderId;
    private Payment payment;
    private PaymentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        payment = new Payment();
        payment.setId("test-id");
        payment.setOrderId(orderId);
        responseDto = new PaymentResponseDto("test-id", orderId, userId, PaymentStatus.SUCCESS, new BigDecimal("100.0"), Instant.now());
    }

    @Test
    void createPayment_Success() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto(orderId, userId, new BigDecimal("100.0"));
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(randomServiceClient.getRandomNumber(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn("2");
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentMapper.toResponse(any())).thenReturn(responseDto);
        when(objectMapper.writeValueAsBytes(any())).thenReturn(new byte[0]);

        PaymentResponseDto result = paymentService.createPayment(request);

        assertThat(result).isNotNull();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        verify(kafkaTemplate).send(eq("payment-results"), eq(orderId.toString()), any());
    }

    @Test
    void createPayment_FailedByRandom() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto(orderId, userId, new BigDecimal("100.0"));
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(randomServiceClient.getRandomNumber(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn("3");
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentMapper.toResponse(any())).thenReturn(new PaymentResponseDto("id", orderId, userId, PaymentStatus.FAILED, BigDecimal.TEN, Instant.now()));

        paymentService.createPayment(request);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void handlePaymentRequest_Success() throws Exception {
        byte[] data = "raw-data".getBytes();
        PaymentRequestDto request = new PaymentRequestDto(orderId, userId, BigDecimal.TEN);
        when(objectMapper.readValue(data, PaymentRequestDto.class)).thenReturn(request);
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(randomServiceClient.getRandomNumber(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn("2");
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentMapper.toResponse(any())).thenReturn(responseDto);

        paymentService.handlePaymentRequest(data);

        verify(objectMapper).readValue(data, PaymentRequestDto.class);
        verify(paymentRepository).save(any());
    }

    @Test
    void getTotalSumForUser_ReturnZeroWhenNull() {
        Instant now = Instant.now();
        when(paymentRepository.getTotalSumByUserIdAndDateRange(anyString(), any(), any())).thenReturn(null);

        BigDecimal result = paymentService.getTotalSumForUser(userId, now, now);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void getPaymentsByStatus_Success() {
        when(paymentRepository.findByStatus(PaymentStatus.SUCCESS)).thenReturn(List.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> results = paymentService.getPaymentsByStatus(PaymentStatus.SUCCESS);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).status()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void sendResultToKafka_ThrowsExceptionOnJacksonError() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto(orderId, userId, BigDecimal.TEN);
        when(paymentMapper.toEntity(any())).thenReturn(payment);
        when(randomServiceClient.getRandomNumber(anyInt(), anyInt(), anyInt(), anyInt(), anyInt(), anyString(), anyString())).thenReturn("2");
        when(paymentRepository.save(any())).thenReturn(payment);
        when(paymentMapper.toResponse(any())).thenReturn(responseDto);

        when(objectMapper.writeValueAsBytes(any())).thenThrow(new RuntimeException("Jackson error"));

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error serializing payment response");
    }

    @Test
    void getPaymentById_Found() {
        String id = "test-id";
        when(paymentRepository.findById(id)).thenReturn(Optional.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(responseDto);

        PaymentResponseDto result = paymentService.getPaymentById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        verify(paymentRepository).findById(id);
    }

    @Test
    void getPaymentById_NotFound() {
        String id = "non-existent";
        when(paymentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(id))
                .isExactlyInstanceOf(java.util.NoSuchElementException.class);
    }

    @Test
    void getPaymentsByUserId_Success() {
        when(paymentRepository.findByUserId(userId)).thenReturn(List.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> results = paymentService.getPaymentsByUserId(userId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).userId()).isEqualTo(userId);
        verify(paymentRepository).findByUserId(userId);
    }

    @Test
    void getPaymentsByOrderId_Success() {
        when(paymentRepository.findByOrderId(orderId)).thenReturn(List.of(payment));
        when(paymentMapper.toResponse(payment)).thenReturn(responseDto);

        List<PaymentResponseDto> results = paymentService.getPaymentsByOrderId(orderId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).orderId()).isEqualTo(orderId);
        verify(paymentRepository).findByOrderId(orderId);
    }

    @Test
    void getTotalSumForAll_Success() {
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        BigDecimal expectedSum = new BigDecimal("500.75");
        when(paymentRepository.getTotalSumForDateRange(start, end)).thenReturn(expectedSum);

        BigDecimal result = paymentService.getTotalSumForAll(start, end);

        assertThat(result).isEqualByComparingTo(expectedSum);
        verify(paymentRepository).getTotalSumForDateRange(start, end);
    }

    @Test
    void getTotalSumForAll_NullResult() {
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        when(paymentRepository.getTotalSumForDateRange(start, end)).thenReturn(null);

        BigDecimal result = paymentService.getTotalSumForAll(start, end);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}