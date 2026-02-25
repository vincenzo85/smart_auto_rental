package com.smartautorental.platform.payment.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.payment-core")
public class PaymentCoreProperties {

    private String mode = "stub";
    private String baseUrl = "http://localhost:8090";
    private String chargePath = "/api/v1/core/payments/charge";
    private String apiKey = "";
    private int connectTimeoutMs = 2000;
    private int readTimeoutMs = 5000;
}
