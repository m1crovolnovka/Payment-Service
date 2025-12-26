package org.example.paymentservice.repository;

import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByUserId(UUID userId);
    List<Payment> findByOrderId(UUID orderId);
    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.userId = :userId " +
            "AND p.timestamp BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSumByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);

    @Query("SELECT SUM(p.paymentAmount) FROM Payment p " +
            "WHERE p.timestamp BETWEEN :startDate AND :endDate")
    BigDecimal getTotalSumForDateRange(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate);
}
