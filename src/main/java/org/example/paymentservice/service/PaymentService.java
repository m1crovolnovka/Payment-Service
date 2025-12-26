package org.example.paymentservice.service;

import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    List<PaymentResponse> getPaymentsByUserId(UUID userId);
    List<PaymentResponse> getPaymentsByOrderId(UUID orderId);
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);

    BigDecimal getTotalSumForUser(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate);
    BigDecimal getTotalSumForAll(OffsetDateTime startDate, OffsetDateTime endDate);
}
