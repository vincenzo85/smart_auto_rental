package com.smartautorental.platform.payment.core;

import com.smartautorental.platform.booking.model.PaymentStatus;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.payment-core.mode", havingValue = "stub", matchIfMissing = true)
public class StubPaymentCoreClient implements PaymentCoreClient {

    @Override
    public PaymentCoreResult charge(PaymentCoreRequest request) {
        return new PaymentCoreResult(
                PaymentStatus.SUCCESS,
                "core-stub-" + UUID.randomUUID()
        );
    }
}
