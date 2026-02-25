package com.smartautorental.platform.admin.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UtilizationResponse(
        Long branchId,
        Instant from,
        Instant to,
        long carsInBranch,
        BigDecimal utilizationPercent
) {
}
