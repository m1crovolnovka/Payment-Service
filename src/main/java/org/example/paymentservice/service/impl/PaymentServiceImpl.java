package org.example.paymentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.paymentservice.client.RandomServiceClient;
import org.example.paymentservice.dto.PaymentEventDto;
import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.mapper.PaymentMapper;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomServiceClient randomServiceClient;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper, RandomServiceClient randomServiceClient, KafkaTemplate<String, byte[]> kafkaTemplate, ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.randomServiceClient = randomServiceClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "payment-requests",
            groupId = "payment-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handlePaymentRequest(byte[] data) {
        try {
            PaymentRequestDto request = objectMapper.readValue(data, PaymentRequestDto.class);
            this.createPayment(request);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing payment request", e);
        }
    }

    @Override
    @Transactional
    public PaymentResponseDto createPayment(PaymentRequestDto request) {
        Payment payment = paymentMapper.toEntity(request);
        try {
            String randomResponse = randomServiceClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new");
            int randomNumber = Integer.parseInt(randomResponse.trim());
            payment.setStatus(randomNumber % 2 == 0 ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
        }
        Payment savedPayment = paymentRepository.save(payment);
        PaymentResponseDto responseDto = paymentMapper.toResponse(savedPayment);
        sendResultToKafka(new PaymentEventDto(responseDto.orderId(), responseDto.status().name()));
        return responseDto;
    }

    private void sendResultToKafka(PaymentEventDto responseDto) {
        try {
            byte[] data = objectMapper.writeValueAsBytes(responseDto);
            kafkaTemplate.send("payment-results", responseDto.getOrderId().toString(), data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializing payment response", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentById(String id) {
        return paymentRepository.findById(id).map(paymentMapper::toResponse).orElseThrow();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSumForUser(UUID userId, Instant startDate, Instant endDate) {
        BigDecimal sum = paymentRepository.getTotalSumByUserIdAndDateRange(userId.toString(), startDate, endDate);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSumForAll(Instant startDate, Instant endDate) {
        BigDecimal sum = paymentRepository.getTotalSumForDateRange(startDate, endDate);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
