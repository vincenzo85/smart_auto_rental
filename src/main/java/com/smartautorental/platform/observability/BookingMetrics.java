package com.smartautorental.platform.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BookingMetrics {

    private final Counter bookingCreated;
    private final Counter bookingFailed;

    public BookingMetrics(MeterRegistry meterRegistry) {
        this.bookingCreated = Counter.builder("booking.created.total")
                .description("Number of created bookings")
                .register(meterRegistry);
        this.bookingFailed = Counter.builder("booking.failed.total")
                .description("Number of booking attempts failed due to conflicts or payments")
                .register(meterRegistry);
    }

    public void incrementCreated() {
        bookingCreated.increment();
    }

    public void incrementFailed() {
        bookingFailed.increment();
    }
}
