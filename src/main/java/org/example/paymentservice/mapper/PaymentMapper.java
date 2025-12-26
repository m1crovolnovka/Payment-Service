package org.example.paymentservice.mapper;

import org.example.paymentservice.dto.PaymentRequest;
import org.example.paymentservice.dto.PaymentResponse;
import org.example.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "status", ignore = true)
    Payment toEntity(PaymentRequest request);

    PaymentResponse toResponse(Payment entity);
}
