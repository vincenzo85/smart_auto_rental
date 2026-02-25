package com.smartautorental.platform.fleet.dto;

import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import java.math.BigDecimal;

public record CarResponse(
        Long id,
        String licensePlate,
        String brand,
        String model,
        CarCategory category,
        Long branchId,
        String branchName,
        CarStatus status,
        BigDecimal baseDailyRate
) {
}
