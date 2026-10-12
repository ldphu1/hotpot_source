package com.fourspicehotpot.backend.modules.checkin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonThanhToanResponse {
    private Long idHoaDon;
    private String maHoaDon;
    private String tenBanAn;
    private String tenKhuVuc;
    private String tenKhachHang;
    private String soDienThoai;
    private LocalDateTime thoiGianTao;

    private Integer trangThaiHoaDon;

    private BigDecimal tienCoc;
    private BigDecimal tongTienMon;
    private BigDecimal soTienDuocGiam;
    private BigDecimal phiThuKhac;
    private BigDecimal vatPercent;
    private BigDecimal tongTien;

    private String maGiamGia;
    private Object phieuGiamGia;

    private BigDecimal tongTienDaThanhToan;

    private List<HoaDonThanhToanItemResponse> dsMon;
}