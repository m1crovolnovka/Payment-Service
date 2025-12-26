package org.example.paymentservice.service;

import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    Payment createPayment(Payment payment);

    List<Payment> getPaymentsByUserId(UUID userId);
    List<Payment> getPaymentsByOrderId(UUID orderId);
    List<Payment> getPaymentsByStatus(PaymentStatus status);

    BigDecimal getTotalSumForUser(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate);
    BigDecimal getTotalSumForAll(OffsetDateTime startDate, OffsetDateTime endDate);
}
