package com.fourspicehotpot.backend.modules.checkin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThanhToanResponse {
    private Long id;
    private String maThanhToan;

    private Long idPhuongThucThanhToan;
    private String maPhuongThuc;
    private String tenPhuongThuc;

    private BigDecimal tongTien;
    private LocalDateTime ngayThanhToan;
    private Integer trangThai;
}