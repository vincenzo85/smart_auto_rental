package com.smartautorental.platform.pricing.service;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CouponService {

    private static final Map<String, BigDecimal> COUPON_PERCENTS = Map.of(
            "WELCOME10", new BigDecimal("0.10"),
            "LONGTRIP5", new BigDecimal("0.05")
    );

    public BigDecimal resolveDiscountPercent(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return BigDecimal.ZERO;
        }
        return COUPON_PERCENTS.getOrDefault(couponCode.toUpperCase(), BigDecimal.ZERO);
    }
}
