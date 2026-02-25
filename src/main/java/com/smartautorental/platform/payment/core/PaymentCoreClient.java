package com.smartautorental.platform.payment.core;

public interface PaymentCoreClient {

    PaymentCoreResult charge(PaymentCoreRequest request);
}
