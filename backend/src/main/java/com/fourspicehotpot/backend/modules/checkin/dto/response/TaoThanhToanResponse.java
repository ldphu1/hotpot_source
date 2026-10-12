package com.fourspicehotpot.backend.modules.checkin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaoThanhToanResponse {
    private Long idThanhToan;
    private String maThanhToan;
    private String paymentUrl;
    private String qrCodeUrl;
    private Integer trangThai;
    private String message;
}
