package org.example.paymentservice.service;

import org.example.paymentservice.dto.PaymentRequestDto;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    PaymentResponseDto createPayment(PaymentRequestDto request);

    List<PaymentResponseDto> getPaymentsByUserId(UUID userId);
    List<PaymentResponseDto> getPaymentsByOrderId(UUID orderId);
    List<PaymentResponseDto> getPaymentsByStatus(PaymentStatus status);

    BigDecimal getTotalSumForUser(UUID userId, OffsetDateTime startDate, OffsetDateTime endDate);
    BigDecimal getTotalSumForAll(OffsetDateTime startDate, OffsetDateTime endDate);
}
