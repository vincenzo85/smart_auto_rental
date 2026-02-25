package com.smartautorental.platform.booking.dto;

import java.time.Instant;

public record BookingAuditResponse(
        String eventType,
        String actor,
        String details,
        Instant timestamp
) {
}
