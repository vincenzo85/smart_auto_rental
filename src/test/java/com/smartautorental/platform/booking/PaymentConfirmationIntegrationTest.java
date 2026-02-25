package com.smartautorental.platform.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartautorental.platform.booking.dto.BookingCreateRequest;
import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentStatus;
import com.smartautorental.platform.booking.service.BookingService;
import com.smartautorental.platform.config.AbstractIntegrationTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PaymentConfirmationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void shouldConfirmBookingWhenPaymentSucceeds() {
        authenticateAs("customer@smartauto.local");

        var response = bookingService.create(new BookingCreateRequest(
                2L,
                Instant.parse("2026-06-10T09:00:00Z"),
                Instant.parse("2026-06-13T09:00:00Z"),
                true,
                "WELCOME10",
                false,
                false
        ));

        assertThat(response.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(response.paymentStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }
}
