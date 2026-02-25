package com.smartautorental.platform.payment.controller;

import com.smartautorental.platform.payment.dto.PaymentRetryResponse;
import com.smartautorental.platform.payment.dto.PaymentTransactionResponse;
import com.smartautorental.platform.payment.dto.PaymentWebhookRequest;
import com.smartautorental.platform.payment.service.PaymentService;
import com.smartautorental.platform.security.CurrentUserService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserService currentUserService;

    @PostMapping("/api/v1/payments/{bookingId}/retry")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public PaymentRetryResponse retryPayment(@PathVariable Long bookingId) {
        var actor = currentUserService.requireCurrentUser();
        return paymentService.retryPayment(bookingId, actor);
    }

    @GetMapping("/api/v1/payments/{bookingId}/transactions")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','OPERATOR')")
    public List<PaymentTransactionResponse> history(@PathVariable Long bookingId) {
        var actor = currentUserService.requireCurrentUser();
        return paymentService.paymentHistory(bookingId, actor).stream()
                .map(tx -> new PaymentTransactionResponse(
                        tx.getId(),
                        tx.getAmount(),
                        tx.getStatus(),
                        tx.getProviderReference(),
                        tx.getCreatedAt()))
                .toList();
    }

    @PostMapping("/api/v1/integrations/payments/webhook")
    public void webhook(@Valid @RequestBody PaymentWebhookRequest request) {
        paymentService.handleWebhook(request);
    }
}
