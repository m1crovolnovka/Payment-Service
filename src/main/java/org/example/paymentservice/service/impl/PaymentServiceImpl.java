package org.example.paymentservice.service.impl;

import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
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

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByUserId(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
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
