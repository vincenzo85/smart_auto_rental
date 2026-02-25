package com.smartautorental.platform.pricing;

import static org.assertj.core.api.Assertions.assertThat;

import com.smartautorental.platform.fleet.model.Branch;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.pricing.dto.PriceQuote;
import com.smartautorental.platform.pricing.service.CouponService;
import com.smartautorental.platform.pricing.service.PricingService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService(Mockito.mock(com.smartautorental.platform.fleet.repo.CarRepository.class), new CouponService());

    @Test
    void shouldApplyDynamicAndCouponPricing() {
        Car car = new Car();
        car.setBaseDailyRate(new BigDecimal("100.00"));
        car.setCategory(CarCategory.SUV);
        Branch branch = new Branch();
        branch.setId(1L);
        car.setBranch(branch);

        Instant start = Instant.parse("2026-03-07T10:00:00Z");
        Instant end = Instant.parse("2026-03-10T10:00:00Z");

        PriceQuote quote = pricingService.quote(car, start, end, true, "WELCOME10", 1);

        assertThat(quote.dynamicFactor()).isEqualByComparingTo("1.15");
        assertThat(quote.total()).isGreaterThan(BigDecimal.ZERO);
        assertThat(quote.couponDiscount()).isGreaterThan(BigDecimal.ZERO);
        assertThat(quote.insuranceFee()).isGreaterThan(BigDecimal.ZERO);
    }
}
