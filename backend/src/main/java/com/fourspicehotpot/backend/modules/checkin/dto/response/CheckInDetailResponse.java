package com.fourspicehotpot.backend.modules.checkin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fourspicehotpot.backend.modules.phieugiamgia.dto.response.PhieuGiamGiaResponse;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckInDetailResponse {
    private Integer idBan;
    private String maBan;
    private String tenBan;
    private Integer sucChuaToiDa;
    private Integer soNguoiDangDung;
    private Integer trangThaiBan;
    private Integer idKhuVuc;
    private String tenKhuVuc;

    private Integer idDatBan;
    private String maDatBan;
    private LocalDateTime thoiGianDat;
    private Integer soNguoiDat;
    private String ghiChuDatBan;
    private BigDecimal tienCoc;
    private String trangThaiCoc;

    private Long idKhachHang;
    private String tenKhachHang;
    private String soDienThoai;

    private Long idHoaDon;
    private String maHoaDon;
    private Integer trangThaiHoaDon;

    // ===== BỔ SUNG PHẦN TIỀN CHO MÀN XEM ĐƠN =====
    private BigDecimal tongTienMon;
    private BigDecimal tongTien;
    private BigDecimal soTienDuocGiam;
    private BigDecimal tienSauGiam;
    private BigDecimal vatPercent;
    private BigDecimal tienConLai;
    private Long idPhieuGiamGia;
    private String maGiamGia;
    private String tenGiamGia;
    private PhieuGiamGiaResponse phieuGiamGia;
    private BigDecimal tongTienDaThanhToan;

    private String trangThaiDatBan;
    private Integer hinhThucDat;
    private LocalDateTime ngayTaoDatBan;
    private LocalDateTime ngayCapNhatDatBan;
    private LocalDateTime hanGiuBan;
    private LocalDateTime hanThanhToanCoc;

    private LocalDateTime thoiGianCheckIn;
    private LocalDateTime thoiGianTaoHoaDon;
    private LocalDateTime thoiGianMo;
    private LocalDateTime thoiGianThanhToan;
    private LocalDateTime thoiGianDong;

    private Long idNhanVien;
    private String tenNhanVien;
    private String nguoiTaoHoaDon;

    private List<Integer> listIdBan;
    private List<String> listTenBan;

    private Integer soHoaDonDangHoatDong;
    private Boolean coNhieuHoaDon;
    private Boolean laBanDungChungNhieuHoaDon;
}