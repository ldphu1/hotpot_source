package com.fourspicehotpot.backend.modules.phieugiamgia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhieuGiamGiaResponse {
    private Integer id;
    private String maGiamGia;
    private String tenGiamGia;
    private Integer phuongThucGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal giamToiDa;
    private BigDecimal dieuKienSuDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer soLuong;
    private Integer kieuGiamGia;
}