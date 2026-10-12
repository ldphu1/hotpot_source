package com.fourspicehotpot.backend.integration.payment.momo.service;

import java.math.BigDecimal;
import java.util.Map;

public interface MomoService {
    Map<String, Object> createPayment(String maThanhToan, BigDecimal amount);

    String verifyCallback(Map<String, Object> body);
}
