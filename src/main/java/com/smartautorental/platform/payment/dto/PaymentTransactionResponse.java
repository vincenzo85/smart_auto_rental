package com.smartautorental.platform.payment.dto;

import com.smartautorental.platform.booking.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentTransactionResponse(
        Long id,
        BigDecimal amount,
        PaymentStatus status,
        String providerReference,
        Instant createdAt
) {
}
