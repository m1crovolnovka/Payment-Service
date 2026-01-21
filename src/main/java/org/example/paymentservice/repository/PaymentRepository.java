package org.example.paymentservice.repository;

import org.example.paymentservice.dto.TotalAmountProjection;
import org.example.paymentservice.entity.Payment;
import org.example.paymentservice.entity.PaymentStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    List<Payment> findByUserId(UUID userId);
    List<Payment> findByOrderId(UUID orderId);
    List<Payment> findByStatus(PaymentStatus status);

//    @Aggregation(pipeline = {
//            "{ '$match': { 'userId': ?0, 'timestamp': { '$gte': ?1, '$lte': ?2 } } }",
//            "{ '$group': { '_id': null, 'total': { '$sum': '$paymentAmount' } } }"
//    })
//    Optional<BigDecimal> getTotalSumByUserIdAndDateRange(String userId, Instant from, Instant to);
//
//    @Aggregation(pipeline = {
//            "{ '$match': { 'timestamp': { '$gte': ?0, '$lte': ?1 } } }",
//            "{ '$group': { '_id': null, 'total': { '$sum': '$paymentAmount' } } }"
//    })
//    Optional<BigDecimal>  getTotalSumForDateRange(Instant start, Instant end);

    List<Payment> findByUserIdAndTimestampBetween(
            UUID userId,
            Instant startDate,
            Instant endDate
    );

    List<Payment> findByTimestampBetween(
            Instant startDate,
            Instant endDate
    );
}

