package com.smartautorental.platform.fleet.dto;

import com.smartautorental.platform.fleet.model.CarCategory;
import java.math.BigDecimal;

public record AvailabilityCarResponse(
        Long carId,
        String licensePlate,
        String brand,
        String model,
        CarCategory category,
        BigDecimal estimatedTotalPrice,
        BigDecimal dynamicFactor
) {
}
