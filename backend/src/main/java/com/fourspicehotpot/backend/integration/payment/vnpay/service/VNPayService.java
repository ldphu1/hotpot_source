package com.fourspicehotpot.backend.integration.payment.vnpay.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    String createPaymentUrl(Long amount, String orderId, HttpServletRequest request);

    String createInvoicePaymentUrl(Long amount, String maThanhToan, HttpServletRequest request); // hóa đơn
}
