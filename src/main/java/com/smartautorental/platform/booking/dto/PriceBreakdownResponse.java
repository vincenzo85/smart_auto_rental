package com.smartautorental.platform.booking.dto;

import java.math.BigDecimal;

public record PriceBreakdownResponse(
        BigDecimal baseAmount,
        BigDecimal weekendSurcharge,
        BigDecimal durationDiscount,
        BigDecimal dynamicSurcharge,
        BigDecimal insuranceFee,
        BigDecimal couponDiscount,
        BigDecimal total
) {
}
