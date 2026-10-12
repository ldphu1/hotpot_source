package com.fourspicehotpot.backend.integration.payment.zalopay.service;

import java.math.BigDecimal;
import java.util.Map;

public interface ZaloPayService {

    Map<String, Object> createInvoiceOrder(String maThanhToan, BigDecimal soTien);

    String verifyAndExtractMaThanhToan(Map<String, Object> callbackBody);
}