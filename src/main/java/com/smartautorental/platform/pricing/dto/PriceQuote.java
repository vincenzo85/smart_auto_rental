package com.smartautorental.platform.pricing.dto;

import java.math.BigDecimal;

public record PriceQuote(
        BigDecimal baseAmount,
        BigDecimal weekendSurcharge,
        BigDecimal durationDiscount,
        BigDecimal dynamicSurcharge,
        BigDecimal insuranceFee,
        BigDecimal couponDiscount,
        BigDecimal total,
        BigDecimal dynamicFactor
) {
}
