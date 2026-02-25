package com.smartautorental.platform.pricing.service;

import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.repo.CarRepository;
import com.smartautorental.platform.pricing.dto.PriceQuote;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PricingService {

    private static final BigDecimal WEEKEND_RATE = new BigDecimal("0.15");
    private static final BigDecimal DURATION_DISCOUNT_RATE = new BigDecimal("0.10");
    private static final BigDecimal DYNAMIC_RATE = new BigDecimal("0.15");
    private static final BigDecimal INSURANCE_PER_DAY = new BigDecimal("20.00");

    private final CarRepository carRepository;
    private final CouponService couponService;

    public PriceQuote quote(Car car,
                            Instant start,
                            Instant end,
                            boolean insuranceSelected,
                            String couponCode,
                            long estimatedAvailableCarsForCategory) {
        long rentalDays = Math.max(1, (long) Math.ceil(Duration.between(start, end).toHours() / 24.0));
        BigDecimal baseAmount = car.getBaseDailyRate().multiply(BigDecimal.valueOf(rentalDays));

        long weekendDays = countWeekendDays(start, end);
        BigDecimal weekendSurcharge = car.getBaseDailyRate()
                .multiply(BigDecimal.valueOf(weekendDays))
                .multiply(WEEKEND_RATE);

        BigDecimal durationDiscount = rentalDays >= 7
                ? baseAmount.multiply(DURATION_DISCOUNT_RATE)
                : BigDecimal.ZERO;

        BigDecimal dynamicFactor = estimatedAvailableCarsForCategory <= 2
                ? BigDecimal.ONE.add(DYNAMIC_RATE)
                : BigDecimal.ONE;

        BigDecimal dynamicSurcharge = baseAmount.multiply(dynamicFactor.subtract(BigDecimal.ONE));
        BigDecimal insuranceFee = insuranceSelected
                ? INSURANCE_PER_DAY.multiply(BigDecimal.valueOf(rentalDays))
                : BigDecimal.ZERO;

        BigDecimal subtotal = baseAmount
                .add(weekendSurcharge)
                .add(dynamicSurcharge)
                .add(insuranceFee)
                .subtract(durationDiscount);

        BigDecimal couponDiscount = subtotal.multiply(couponService.resolveDiscountPercent(couponCode));

        BigDecimal total = subtotal.subtract(couponDiscount).setScale(2, RoundingMode.HALF_UP);

        return new PriceQuote(
                scale(baseAmount),
                scale(weekendSurcharge),
                scale(durationDiscount),
                scale(dynamicSurcharge),
                scale(insuranceFee),
                scale(couponDiscount),
                total,
                dynamicFactor.setScale(2, RoundingMode.HALF_UP));
    }

    public long estimateAvailableCarsForCategory(Long branchId,
                                                  com.smartautorental.platform.fleet.model.CarCategory category) {
        return carRepository.countByBranchIdAndCategoryAndStatus(branchId, category, CarStatus.AVAILABLE);
    }

    private long countWeekendDays(Instant start, Instant end) {
        ZonedDateTime cursor = start.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime limit = end.atZone(ZoneOffset.UTC).toLocalDate().plusDays(1).atStartOfDay(ZoneOffset.UTC);

        long weekendDays = 0;
        while (cursor.isBefore(limit)) {
            DayOfWeek day = cursor.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
            cursor = cursor.plusDays(1);
        }

        return weekendDays;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
