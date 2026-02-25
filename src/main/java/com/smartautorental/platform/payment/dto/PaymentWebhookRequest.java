package com.smartautorental.platform.payment.dto;

import com.smartautorental.platform.booking.model.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentWebhookRequest(
        @NotNull Long bookingId,
        @NotNull PaymentStatus status,
        @NotBlank String providerReference
) {
}
