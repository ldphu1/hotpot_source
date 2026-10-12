package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaoLinkThanhToanRequest {
    private Long idHoaDon;
    private Long idPhuongThucThanhToan; // VNPAY / MOMO / ZALOPAY
    private BigDecimal soTien;
    private String returnUrl;
}
