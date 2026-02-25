package com.smartautorental.platform.maintenance.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MaintenanceCreateRequest(
        @NotNull Long carId,
        @NotNull @Future Instant startTime,
        @NotNull @Future Instant endTime,
        @NotBlank String description
) {
}
