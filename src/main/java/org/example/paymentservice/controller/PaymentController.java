package org.example.paymentservice.controller;

import org.example.paymentservice.dto.PaymentResponseDto;
import org.example.paymentservice.entity.PaymentStatus;
import org.example.paymentservice.service.PaymentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('ADMIN') or @ps.isPaymentOwner(#paymentId, authentication.name)")
    public ResponseEntity<PaymentResponseDto> getPaymentById(@PathVariable String paymentId) {
        return ResponseEntity.ok(paymentService.getPaymentById(paymentId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAuthority('ADMIN') or @ps.isOrderOwner(#orderId, authentication.name)")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByOrderId(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentsByStatus(@RequestParam PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @GetMapping("/sum/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<BigDecimal> getTotalSumForUser(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        return ResponseEntity.ok(paymentService.getTotalSumForUser(userId, startDate, endDate));
    }

    @GetMapping("/sum/total")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BigDecimal> getTotalSumForAll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {

        return ResponseEntity.ok(paymentService.getTotalSumForAll(startDate, endDate));
    }
}
