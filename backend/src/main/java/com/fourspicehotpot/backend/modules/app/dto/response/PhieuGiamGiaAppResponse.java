package com.fourspicehotpot.backend.modules.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaAppResponse {
    private Integer id;
    private String maGiamGia;
    private String tenGiamGia;
    private Integer kieuGiamGia; // 1 công khai, 2 cá nhân
    private Integer phuongThucGiamGia; // 1 phần trăm, 2 tiền mặt
    private BigDecimal giaTriGiam;
    private BigDecimal giamToiDa;
    private BigDecimal dieuKienSuDung; // Đơn tối thiểu
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
}
