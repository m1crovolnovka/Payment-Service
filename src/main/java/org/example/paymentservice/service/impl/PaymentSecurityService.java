package org.example.paymentservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service("ps")
@RequiredArgsConstructor
public class PaymentSecurityService {
    private final PaymentService paymentService;

    public boolean isPaymentOwner(String paymentId, String principalId) {
        PaymentResponseDto payment = paymentService.getPaymentById(paymentId);
        return payment.userId().toString().equals(principalId);
    }

    public boolean isOrderOwner(UUID orderId, String principalId) {
        List<PaymentResponseDto> payments = paymentService.getPaymentsByOrderId(orderId);
        return payments.stream()
                .anyMatch(p -> p.userId().toString().equals(principalId));
    }
}