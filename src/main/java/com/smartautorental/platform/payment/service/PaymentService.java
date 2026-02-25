package com.smartautorental.platform.payment.service;

import com.smartautorental.platform.booking.model.Booking;
import com.smartautorental.platform.booking.model.BookingStatus;
import com.smartautorental.platform.booking.model.PaymentStatus;
import com.smartautorental.platform.booking.repo.BookingRepository;
import com.smartautorental.platform.booking.service.BookingAuditService;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import com.smartautorental.platform.identity.model.User;
import com.smartautorental.platform.identity.model.UserRole;
import com.smartautorental.platform.notification.NotificationService;
import com.smartautorental.platform.payment.dto.PaymentResult;
import com.smartautorental.platform.payment.dto.PaymentRetryResponse;
import com.smartautorental.platform.payment.dto.PaymentWebhookRequest;
import com.smartautorental.platform.payment.model.PaymentTransaction;
import com.smartautorental.platform.payment.repo.PaymentTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRepository;
    private final BookingAuditService bookingAuditService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentResult processInitialPayment(Booking booking, PaymentStatus forcedStatus) {
        PaymentStatus status = resolveOutcome(forcedStatus);
        PaymentTransaction tx = saveTransaction(booking, booking.getTotalPrice(), status);
        return new PaymentResult(status, tx.getProviderReference());
    }

    @Transactional
    public PaymentRetryResponse retryPayment(Long bookingId, PaymentStatus forcedStatus, User actor) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));

        if (actor.getRole() == UserRole.CUSTOMER && !booking.getCustomer().getId().equals(actor.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Cannot retry payment for another customer booking");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "Booking is already confirmed");
        }

        PaymentResult result = processInitialPayment(booking, forcedStatus);
        applyPaymentOutcome(booking, result.status());
        bookingAuditService.log(bookingId, "PAYMENT_RETRY", actor.getEmail(),
                "Retry payment result=" + result.status() + " providerRef=" + result.providerReference());

        return new PaymentRetryResponse(bookingId, booking.getStatus(), booking.getPaymentStatus(), result.providerReference());
    }

    @Transactional
    public void handleWebhook(PaymentWebhookRequest request) {
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Booking not found"));

        saveTransaction(booking, booking.getTotalPrice(), request.status(), request.providerReference());
        applyPaymentOutcome(booking, request.status());
        bookingAuditService.log(booking.getId(), "PAYMENT_WEBHOOK", "webhook",
                "Webhook update status=" + request.status() + " providerRef=" + request.providerReference());

        if (request.status() == PaymentStatus.SUCCESS) {
            notificationService.send(booking.getCustomer().getEmail(),
                    "Booking confirmed",
                    "Booking " + booking.getCode() + " confirmed via webhook");
        }
    }

    @Transactional(readOnly = true)
    public List<PaymentTransaction> paymentHistory(Long bookingId) {
        return paymentTransactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
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

    private PaymentTransaction saveTransaction(Booking booking, BigDecimal amount, PaymentStatus status) {
        return saveTransaction(booking, amount, status, "mock-" + UUID.randomUUID());
    }

    private PaymentTransaction saveTransaction(Booking booking,
                                               BigDecimal amount,
                                               PaymentStatus status,
                                               String providerReference) {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setBooking(booking);
        tx.setAmount(amount);
        tx.setStatus(status);
        tx.setProviderReference(providerReference);
        return paymentTransactionRepository.save(tx);
    }

    private PaymentStatus resolveOutcome(PaymentStatus forcedStatus) {
        if (forcedStatus != null) {
            return forcedStatus;
        }

        double random = Math.random();
        if (random < 0.70) {
            return PaymentStatus.SUCCESS;
        }
        if (random < 0.90) {
            return PaymentStatus.PENDING;
        }
        return PaymentStatus.FAILED;
    }
}
