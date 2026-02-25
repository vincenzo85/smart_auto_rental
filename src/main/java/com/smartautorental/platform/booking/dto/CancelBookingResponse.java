package com.smartautorental.platform.booking.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CancelBookingResponse(
        Long bookingId,
        String status,
        BigDecimal cancellationFee,
        BigDecimal refundAmount,
        Instant cancelledAt
) {
}
