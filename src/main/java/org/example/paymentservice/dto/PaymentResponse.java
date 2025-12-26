package org.example.paymentservice.dto;

import org.example.paymentservice.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID userId,
        PaymentStatus status,
        BigDecimal paymentAmount,
        OffsetDateTime timestamp
) {}
