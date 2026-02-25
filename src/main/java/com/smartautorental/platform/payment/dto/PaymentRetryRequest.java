package com.smartautorental.platform.payment.dto;

import com.smartautorental.platform.booking.model.PaymentStatus;

public record PaymentRetryRequest(
        PaymentStatus forcedStatus
) {
}
