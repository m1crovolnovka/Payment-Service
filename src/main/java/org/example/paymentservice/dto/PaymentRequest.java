package org.example.paymentservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0", message = "Amount must be greater or equals than 0")
        BigDecimal paymentAmount
) {}
