package com.smartautorental.platform.fleet.dto;

import com.smartautorental.platform.fleet.model.CarCategory;
import com.smartautorental.platform.fleet.model.CarStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CarUpsertRequest(
        @NotBlank String licensePlate,
        @NotBlank String brand,
        @NotBlank String model,
        @NotNull CarCategory category,
        @NotNull Long branchId,
        @NotNull CarStatus status,
        @NotNull @DecimalMin("1.0") BigDecimal baseDailyRate
) {
}
