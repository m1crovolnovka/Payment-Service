package org.example.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequestDto(
        UUID orderId,
        UUID userId,
        BigDecimal paymentAmount
) {}
