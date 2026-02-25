package com.smartautorental.platform.payment.dto;

import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentStatus;

public record PaymentRetryResponse(
        Long bookingId,
        BookingStatus bookingStatus,
        PaymentStatus paymentStatus,
        String providerReference
) {
}
