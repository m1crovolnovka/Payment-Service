package org.example.paymentservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Field(targetType = FieldType.STRING)
    private UUID orderId;

    @Field(targetType = FieldType.STRING)
    private UUID userId;

    private PaymentStatus status;

    private BigDecimal paymentAmount;

    @Builder.Default
    private Instant timestamp = Instant.now();
}
