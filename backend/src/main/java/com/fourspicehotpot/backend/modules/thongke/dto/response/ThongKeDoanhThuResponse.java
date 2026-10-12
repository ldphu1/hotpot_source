package com.fourspicehotpot.backend.modules.thongke.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ThongKeDoanhThuResponse {
    private BigDecimal doanhThuHomNay;
    private BigDecimal doanhThuDuKienHomNay;
    private BigDecimal doanhThuHomQua;

    private BigDecimal doanhThuTuanNay;
    private BigDecimal doanhThuDuKienTuanNay;
    private BigDecimal doanhThuTuanTruoc;

    private BigDecimal doanhThuThangNay;
    private BigDecimal doanhThuDuKienThangNay;
    private BigDecimal doanhThuThangTruoc;

    private BigDecimal doanhThuNamNay;
    private BigDecimal doanhThuDuKienNamNay;
    private BigDecimal doanhThuNamTruoc;


}
