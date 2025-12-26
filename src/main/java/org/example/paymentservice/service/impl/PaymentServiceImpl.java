package org.example.paymentservice.service.impl;

import org.example.paymentservice.client.RandomServiceClient;
import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.mapper.PaymentMapper;
import org.example.paymentservice.repository.PaymentRepository;
import org.example.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RandomServiceClient randomServiceClient;

    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentMapper paymentMapper, RandomServiceClient randomServiceClient) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
        this.randomServiceClient = randomServiceClient;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        String response = randomServiceClient.getRandomNumber(1, 1, 100, 1, 10, "plain", "new");
        int randomNumber = Integer.parseInt(response.trim());
        PaymentStatus status = (randomNumber % 2 == 0) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
        payment.setStatus(status);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toResponse(savedPayment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSumForUser(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        BigDecimal sum = paymentRepository.getTotalSumByUserIdAndDateRange(userId, startDate, endDate);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalSumForAll(OffsetDateTime startDate, OffsetDateTime endDate) {
        BigDecimal sum = paymentRepository.getTotalSumForDateRange(startDate, endDate);
        return sum != null ? sum : BigDecimal.ZERO;
    }
}
