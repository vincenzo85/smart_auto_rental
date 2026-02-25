package com.smartautorental.platform.fleet;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartautorental.platform.config.AbstractIntegrationTest;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.service.AvailabilityService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AvailabilityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Test
    void shouldReturnAvailableCarsForDateRange() {
        authenticateAs("customer@smartauto.local");

        var result = availabilityService.search(
                1L,
                Instant.parse("2026-04-10T10:00:00Z"),
                Instant.parse("2026-04-12T10:00:00Z"),
                CarCategory.ECONOMY);

        assertThat(result).isNotEmpty();
        assertThat(result.getFirst().estimatedTotalPrice()).isPositive();
    }
}
