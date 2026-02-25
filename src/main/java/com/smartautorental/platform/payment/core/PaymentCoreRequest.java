package com.smartautorental.platform.payment.core;

import java.math.BigDecimal;

public record PaymentCoreRequest(
        Long bookingId,
        String bookingCode,
        BigDecimal amount,
        String currency,
        PaymentAttemptType attemptType
) {
}
