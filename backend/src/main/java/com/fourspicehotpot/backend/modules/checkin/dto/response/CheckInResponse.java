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
public class CheckInResponse {

    // ===== BÀN ĂN =====
    private Integer idBanAn;
    private String maBanAn;
    private String tenBanAn;
    private Integer sucChuaToiDa;
    private Integer trangThaiBan;

    private Integer khuVucId;
    private String tenKhuVuc;

    // ===== ĐẶT BÀN =====
    private Integer idDatBan;
    private String maDatBan;
    private String tenKhachHang;
    private String soDienThoai;
    private LocalDateTime thoiGianDat;
    private Integer soNguoiToiDa;
    private String trangThaiDatBan;
    private String ghiChuDatBan;
    private Integer hinhThucDat;

    // ===== HÓA ĐƠN =====
    private Long idHoaDon;
    private String maHoaDon;
    private Integer trangThaiHoaDon;

    // ===== THÔNG BÁO =====
    private String message;

    private LocalDateTime hanGiuBan;
    private String trangThaiCoc;
    private LocalDateTime thoiGianCheckIn;
    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianThanhToan;

    private BigDecimal tienCoc;
    private BigDecimal tongTienMon;
    private BigDecimal tongTien;
    private BigDecimal soTienDuocGiam;
    private BigDecimal tienConLai;
    private Long idPhieuGiamGia;
    private String maGiamGia;

    private List<Integer> listIdBan;
    private List<String> listTenBan;

    private Integer soHoaDonDangHoatDong;
    private Boolean coNhieuHoaDon;
    private Boolean laBanDungChungNhieuHoaDon;

    private Integer soNguoiDangDung;
    private Integer soGheTrong;
}