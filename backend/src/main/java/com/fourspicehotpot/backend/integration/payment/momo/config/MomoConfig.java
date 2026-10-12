package com.fourspicehotpot.backend.integration.payment.momo.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class MomoConfig {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.createUrl}")
    private String createUrl;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;
}