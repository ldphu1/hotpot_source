package com.fourspicehotpot.backend.integration.payment.zalopay.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ZaloPayConfig {

    @Value("${zalopay.app-id}")
    private String appId;

    @Value("${zalopay.key1}")
    private String key1;

    @Value("${zalopay.key2}")
    private String key2;

    @Value("${zalopay.create-order-url}")
    private String createOrderUrl;

    @Value("${zalopay.callback-url}")
    private String callbackUrl;

    @Value("${zalopay.redirect-url}")
    private String redirectUrl;
}