package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TaoThanhToanRequest {
    private Long idHoaDon;
    private Long idPhuongThucThanhToan;
    private BigDecimal soTien;
    private String ghiChu;
}
