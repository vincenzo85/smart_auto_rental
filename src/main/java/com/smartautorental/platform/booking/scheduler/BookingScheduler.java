package com.smartautorental.platform.booking.scheduler;

import com.smartautorental.platform.booking.service.BookingService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;

    @Scheduled(fixedDelayString = "${app.scheduler.pending-booking-expiration-ms:60000}")
    public void expirePendingBookings() {
        Instant threshold = Instant.now().minusSeconds(15 * 60);
        int expired = bookingService.expirePendingBookings(threshold);
        if (expired > 0) {
            log.info("Expired {} pending bookings", expired);
        }
    }
}
