package com.smartautorental.platform.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.smartautorental.platform.booking.dto.BookingCreateRequest;
import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentStatus;
import com.smartautorental.platform.booking.service.BookingService;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.config.AbstractIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BookingConflictIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void shouldRejectConflictingBookingWithoutWaitlist() {
        authenticateAs("customer@smartauto.local");

        BookingCreateRequest request = new BookingCreateRequest(
                1L,
                Instant.parse("2026-05-01T10:00:00Z"),
                Instant.parse("2026-05-03T10:00:00Z"),
                false,
                null,
                false,
                false,
                PaymentStatus.SUCCESS
        );

        var first = bookingService.create(request);
        assertThat(first.status()).isEqualTo(BookingStatus.CONFIRMED);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(BusinessException.class)
                .extracting(ex -> ((BusinessException) ex).getErrorCode())
                .isEqualTo(ErrorCode.CONFLICT);
    }
}
