package com.smartautorental.platform.booking.model;

import com.smartautorental.platform.common.model.BaseEntity;
import com.smartautorental.platform.fleet.model.Branch;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.identity.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 20)
    private PaymentMode paymentMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "insurance_selected", nullable = false)
    private boolean insuranceSelected;

    @Column(name = "coupon_code", length = 40)
    private String couponCode;

    @Column(name = "base_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "weekend_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal weekendSurcharge = BigDecimal.ZERO;

    @Column(name = "duration_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal durationDiscount = BigDecimal.ZERO;

    @Column(name = "dynamic_surcharge", nullable = false, precision = 10, scale = 2)
    private BigDecimal dynamicSurcharge = BigDecimal.ZERO;

    @Column(name = "insurance_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal insuranceFee = BigDecimal.ZERO;

    @Column(name = "coupon_discount", nullable = false, precision = 10, scale = 2)
    private BigDecimal couponDiscount = BigDecimal.ZERO;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancellation_fee", precision = 10, scale = 2)
    private BigDecimal cancellationFee = BigDecimal.ZERO;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;
}
