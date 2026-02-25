package com.smartautorental.platform.booking.service;

import com.smartautorental.platform.booking.dto.BookingAuditResponse;
import com.smartautorental.platform.booking.dto.BookingCreateRequest;
import com.smartautorental.platform.booking.dto.BookingResponse;
import com.smartautorental.platform.booking.dto.CancelBookingResponse;
import com.smartautorental.platform.booking.dto.PriceBreakdownResponse;
import com.smartautorental.platform.booking.model.Booking;
import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentMode;
import com.smartautorental.platform.booking.model.PaymentStatus;
import com.smartautorental.platform.booking.model.WaitlistEntry;
import com.smartautorental.platform.booking.model.WaitlistStatus;
import com.smartautorental.platform.booking.repo.BookingAuditRepository;
import com.smartautorental.platform.booking.repo.BookingRepository;
import com.smartautorental.platform.booking.repo.WaitlistRepository;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.fleet.model.Car;
import com.smartautorental.platform.fleet.model.CarStatus;
import com.smartautorental.platform.fleet.repo.CarRepository;
import com.smartautorental.platform.identity.model.User;
import com.smartautorental.platform.identity.model.UserRole;
import com.smartautorental.platform.maintenance.service.MaintenanceService;
import com.smartautorental.platform.notification.NotificationService;
import com.smartautorental.platform.observability.BookingMetrics;
import com.smartautorental.platform.payment.dto.PaymentResult;
import com.smartautorental.platform.payment.service.PaymentService;
import com.smartautorental.platform.pricing.dto.PriceQuote;
import com.smartautorental.platform.pricing.service.PricingService;
import com.smartautorental.platform.security.CurrentUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final WaitlistRepository waitlistRepository;
    private final BookingAuditRepository bookingAuditRepository;
    private final CarRepository carRepository;
    private final MaintenanceService maintenanceService;
    private final PricingService pricingService;
    private final CurrentUserService currentUserService;
    private final PaymentService paymentService;
    private final BookingAuditService bookingAuditService;
    private final NotificationService notificationService;
    private final BookingMetrics bookingMetrics;

    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        validateDates(request.startTime(), request.endTime());
        User customer = currentUserService.requireCurrentUser();

        Car car = carRepository.findByIdForUpdate(request.carId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Car not found"));

        if (car.getStatus() == CarStatus.DISABLED || car.getStatus() == CarStatus.IN_MAINTENANCE) {
            return handleUnavailableCar(request, customer, car, "Car is not operational");
        }

        boolean underMaintenance = maintenanceService.isCarUnderMaintenance(car.getId(), request.startTime(), request.endTime());
        boolean conflict = bookingRepository.existsConflictingBooking(
                car.getId(),
                request.startTime(),
                request.endTime(),
                List.of(BookingStatus.CONFIRMED, BookingStatus.PENDING_PAYMENT)
        );

        if (underMaintenance || conflict) {
            String reason = underMaintenance ? "Car in maintenance for selected window" : "Car already booked in selected window";
            return handleUnavailableCar(request, customer, car, reason);
        }

        long availableCars = pricingService.estimateAvailableCarsForCategory(car.getBranch().getId(), car.getCategory());
        PriceQuote quote = pricingService.quote(
                car,
                request.startTime(),
                request.endTime(),
                request.insuranceSelected(),
                request.couponCode(),
                availableCars);

        Booking booking = new Booking();
        booking.setCode("BKG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        booking.setCustomer(customer);
        booking.setCar(car);
        booking.setBranch(car.getBranch());
        booking.setStartTime(request.startTime());
        booking.setEndTime(request.endTime());
        booking.setInsuranceSelected(request.insuranceSelected());
        booking.setCouponCode(request.couponCode());
        booking.setBaseAmount(quote.baseAmount());
        booking.setWeekendSurcharge(quote.weekendSurcharge());
        booking.setDurationDiscount(quote.durationDiscount());
        booking.setDynamicSurcharge(quote.dynamicSurcharge());
        booking.setInsuranceFee(quote.insuranceFee());
        booking.setCouponDiscount(quote.couponDiscount());
        booking.setTotalPrice(quote.total());

        PaymentMode paymentMode = request.payAtDesk() ? PaymentMode.PAY_AT_DESK : PaymentMode.ONLINE;
        booking.setPaymentMode(paymentMode);

        if (request.payAtDesk()) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PENDING);
        } else {
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
            booking.setPaymentStatus(PaymentStatus.PENDING);
        }

        booking = bookingRepository.save(booking);
        bookingAuditService.log(booking.getId(), "BOOKING_CREATED", customer.getEmail(),
                "Booking created for car=" + car.getLicensePlate());

        if (!request.payAtDesk()) {
            PaymentResult paymentResult = paymentService.processInitialPayment(booking);
            applyPaymentOutcome(booking, paymentResult.status());
            bookingAuditService.log(booking.getId(), "PAYMENT_ATTEMPT", customer.getEmail(),
                    "Payment status=" + paymentResult.status() + " ref=" + paymentResult.providerReference());
        }

        Booking saved = bookingRepository.save(booking);

        if (saved.getStatus() == BookingStatus.CONFIRMED) {
            bookingMetrics.incrementCreated();
            notificationService.send(
                    saved.getCustomer().getEmail(),
                    "Booking confirmed",
                    "Your booking " + saved.getCode() + " is confirmed");
        } else {
            bookingMetrics.incrementFailed();
        }

        return map(saved, null);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long bookingId) {
        Booking booking = bookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));

        User current = currentUserService.requireCurrentUser();
        if (current.getRole() == UserRole.CUSTOMER && !booking.getCustomer().getId().equals(current.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot access this booking");
        }

        return map(booking, null);
    }

    @Transactional(readOnly = true)
    public BookingResponse getByIdForIntegration(Long bookingId) {
        Booking booking = bookingRepository.findDetailedById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));
        return map(booking, null);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings() {
        User current = currentUserService.requireCurrentUser();
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(current.getId()).stream()
                .map(booking -> map(booking, null))
                .toList();
    }

    @Transactional
    public CancelBookingResponse cancel(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));

        User current = currentUserService.requireCurrentUser();
        if (current.getRole() == UserRole.CUSTOMER && !booking.getCustomer().getId().equals(current.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot cancel this booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Booking is already closed");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(Instant.now());

        long hoursUntilStart = Duration.between(Instant.now(), booking.getStartTime()).toHours();
        BigDecimal cancellationFee = hoursUntilStart < 24
                ? booking.getTotalPrice().multiply(new BigDecimal("0.30"))
                : BigDecimal.ZERO;
        cancellationFee = cancellationFee.setScale(2, RoundingMode.HALF_UP);

        BigDecimal refund = booking.getTotalPrice().subtract(cancellationFee).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        booking.setCancellationFee(cancellationFee);
        booking.setRefundAmount(refund);

        bookingAuditService.log(booking.getId(), "BOOKING_CANCELLED", current.getEmail(),
                "Cancellation fee=" + cancellationFee + " refund=" + refund);

        promoteWaitlistIfPresent(booking);

        return new CancelBookingResponse(
                booking.getId(),
                booking.getStatus().name(),
                cancellationFee,
                refund,
                booking.getCancelledAt());
    }

    @Transactional(readOnly = true)
    public List<BookingAuditResponse> auditTrail(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));

        User current = currentUserService.requireCurrentUser();
        if (current.getRole() == UserRole.CUSTOMER && !booking.getCustomer().getId().equals(current.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot access this booking audit");
        }

        return bookingAuditRepository.findByBookingIdOrderByCreatedAtAsc(bookingId).stream()
                .map(a -> new BookingAuditResponse(a.getEventType(), a.getActor(), a.getDetails(), a.getCreatedAt()))
                .toList();
    }

    @Transactional
    public int expirePendingBookings(Instant threshold) {
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.PENDING_PAYMENT, threshold);
        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingAuditService.log(booking.getId(), "BOOKING_EXPIRED", "scheduler", "Pending booking expired");
        }
        return expired.size();
    }

    private void applyPaymentOutcome(Booking booking, PaymentStatus status) {
        booking.setPaymentStatus(status);
        if (status == PaymentStatus.SUCCESS) {
            booking.setStatus(BookingStatus.CONFIRMED);
        } else if (status == PaymentStatus.FAILED) {
            booking.setStatus(BookingStatus.PAYMENT_FAILED);
        } else {
            booking.setStatus(BookingStatus.PENDING_PAYMENT);
        }
    }

    private BookingResponse handleUnavailableCar(BookingCreateRequest request, User customer, Car car, String reason) {
        bookingMetrics.incrementFailed();
        if (!request.allowWaitlist()) {
            throw new BusinessException(ErrorCode.CONFLICT, reason + ". Enable allowWaitlist to queue request.");
        }

        WaitlistEntry waitlistEntry = new WaitlistEntry();
        waitlistEntry.setCustomer(customer);
        waitlistEntry.setBranch(car.getBranch());
        waitlistEntry.setCategory(car.getCategory());
        waitlistEntry.setStartTime(request.startTime());
        waitlistEntry.setEndTime(request.endTime());
        waitlistEntry.setStatus(WaitlistStatus.PENDING);
        WaitlistEntry saved = waitlistRepository.save(waitlistEntry);

        notificationService.send(customer.getEmail(), "Waitlist activated",
                "You are in waitlist for category " + car.getCategory() + " at branch " + car.getBranch().getName());

        return new BookingResponse(
                null,
                null,
                BookingStatus.WAITLISTED,
                request.payAtDesk() ? PaymentMode.PAY_AT_DESK : PaymentMode.ONLINE,
                PaymentStatus.PENDING,
                customer.getId(),
                car.getId(),
                car.getLicensePlate(),
                car.getBranch().getId(),
                request.startTime(),
                request.endTime(),
                request.insuranceSelected(),
                request.couponCode(),
                new PriceBreakdownResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                saved.getId());
    }

    private void promoteWaitlistIfPresent(Booking cancelledBooking) {
        List<WaitlistEntry> entries = waitlistRepository
                .findByBranchIdAndCategoryAndStatusAndStartTimeLessThanAndEndTimeGreaterThanOrderByCreatedAtAsc(
                        cancelledBooking.getBranch().getId(),
                        cancelledBooking.getCar().getCategory(),
                        WaitlistStatus.PENDING,
                        cancelledBooking.getEndTime(),
                        cancelledBooking.getStartTime());

        if (!entries.isEmpty()) {
            WaitlistEntry entry = entries.getFirst();
            entry.setStatus(WaitlistStatus.FULFILLED);
            notificationService.send(entry.getCustomer().getEmail(),
                    "Car available from waitlist",
                    "A car became available for your requested period. Proceed with a new booking.");
        }
    }

    private void validateDates(Instant startTime, Instant endTime) {
        if (startTime.isBefore(Instant.now())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Booking cannot start in the past");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Booking end time must be after start time");
        }
    }

    private BookingResponse map(Booking booking, Long waitlistEntryId) {
        return new BookingResponse(
                booking.getId(),
                booking.getCode(),
                booking.getStatus(),
                booking.getPaymentMode(),
                booking.getPaymentStatus(),
                booking.getCustomer().getId(),
                booking.getCar().getId(),
                booking.getCar().getLicensePlate(),
                booking.getBranch().getId(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.isInsuranceSelected(),
                booking.getCouponCode(),
                new PriceBreakdownResponse(
                        booking.getBaseAmount(),
                        booking.getWeekendSurcharge(),
                        booking.getDurationDiscount(),
                        booking.getDynamicSurcharge(),
                        booking.getInsuranceFee(),
                        booking.getCouponDiscount(),
                        booking.getTotalPrice()),
                waitlistEntryId);
    }
}
