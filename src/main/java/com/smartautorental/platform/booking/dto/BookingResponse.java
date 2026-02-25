package com.smartautorental.platform.booking.dto;

import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentMode;
import com.smartautorental.platform.booking.model.PaymentStatus;
import java.time.Instant;

public record BookingResponse(
        Long id,
        String code,
        BookingStatus status,
        PaymentMode paymentMode,
        PaymentStatus paymentStatus,
        Long customerId,
        Long carId,
        String licensePlate,
        Long branchId,
        Instant startTime,
        Instant endTime,
        boolean insuranceSelected,
        String couponCode,
        PriceBreakdownResponse price,
        Long waitlistEntryId
) {
}
