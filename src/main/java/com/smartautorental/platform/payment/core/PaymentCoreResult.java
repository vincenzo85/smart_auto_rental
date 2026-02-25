package com.smartautorental.platform.payment.core;

import com.smartautorental.platform.booking.model.PaymentStatus;

public record PaymentCoreResult(
        PaymentStatus status,
        String providerReference
) {
}
