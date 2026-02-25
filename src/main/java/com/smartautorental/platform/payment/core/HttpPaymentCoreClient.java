package com.smartautorental.platform.payment.core;

import com.smartautorental.platform.booking.model.PaymentStatus;
import com.smartautorental.platform.common.exception.BusinessException;
import com.smartautorental.platform.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.payment-core.mode", havingValue = "http")
public class HttpPaymentCoreClient implements PaymentCoreClient {

    private final RestTemplate paymentCoreRestTemplate;
    private final PaymentCoreProperties paymentCoreProperties;

    @Override
    public PaymentCoreResult charge(PaymentCoreRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (paymentCoreProperties.getApiKey() != null && !paymentCoreProperties.getApiKey().isBlank()) {
                headers.set("X-API-KEY", paymentCoreProperties.getApiKey());
            }

            CoreChargeRequest payload = new CoreChargeRequest(
                    request.bookingId(),
                    request.bookingCode(),
                    request.amount(),
                    request.currency(),
                    request.attemptType().name()
            );

            HttpEntity<CoreChargeRequest> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<CoreChargeResponse> response = paymentCoreRestTemplate.postForEntity(
                    buildChargeUrl(),
                    entity,
                    CoreChargeResponse.class
            );

            CoreChargeResponse body = response.getBody();
            if (body == null || body.status() == null || body.providerReference() == null || body.providerReference().isBlank()) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Payment core returned an invalid response");
            }

            return new PaymentCoreResult(body.status(), body.providerReference());
        } catch (RestClientException ex) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Payment core is unavailable");
        }
    }

    private String buildChargeUrl() {
        String baseUrl = paymentCoreProperties.getBaseUrl();
        String chargePath = paymentCoreProperties.getChargePath();

        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalizedPath = chargePath.startsWith("/") ? chargePath : "/" + chargePath;
        return normalizedBase + normalizedPath;
    }

    private record CoreChargeRequest(
            Long bookingId,
            String bookingCode,
            java.math.BigDecimal amount,
            String currency,
            String attemptType
    ) {
    }

    private record CoreChargeResponse(
            PaymentStatus status,
            String providerReference
    ) {
    }
}
