package com.smartautorental.platform.payment.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PaymentCoreHttpConfig {

    @Bean
    @ConditionalOnProperty(name = "app.payment-core.mode", havingValue = "http")
    public RestTemplate paymentCoreRestTemplate(PaymentCoreProperties paymentCoreProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(paymentCoreProperties.getConnectTimeoutMs());
        requestFactory.setReadTimeout(paymentCoreProperties.getReadTimeoutMs());
        return new RestTemplate(requestFactory);
    }
}
