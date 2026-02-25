package com.smartautorental.platform.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record BookingCreateRequest(
        @NotNull Long carId,
        @NotNull @Future Instant startTime,
        @NotNull @Future Instant endTime,
        boolean insuranceSelected,
        String couponCode,
        boolean payAtDesk,
        boolean allowWaitlist
) {
}
