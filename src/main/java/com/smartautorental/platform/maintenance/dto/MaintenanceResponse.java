package com.smartautorental.platform.maintenance.dto;

import com.smartautorental.platform.maintenance.model.MaintenanceStatus;
import java.time.Instant;

public record MaintenanceResponse(
        Long id,
        Long carId,
        String licensePlate,
        Instant startTime,
        Instant endTime,
        String description,
        MaintenanceStatus status
) {
}
