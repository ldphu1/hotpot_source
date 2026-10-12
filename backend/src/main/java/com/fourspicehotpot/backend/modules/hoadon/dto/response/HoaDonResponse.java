package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import com.fourspicehotpot.backend.common.enums.HinhThucCoc;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonResponse {

    private Long id;

    private String maHoaDon;

    private Integer loaiHoaDon;

    private LocalDateTime thoiGianTao;

    private LocalDateTime thoiGianThanhToan;

    private BigDecimal tienCoc;

    private BigDecimal tienThue;

    private String tenNguoiNhan;

    private String soDienThoai;

    private String diaChiGiao;

    private BigDecimal phiVanChuyen;

    private BigDecimal soTienDuocGiam;

    private BigDecimal tongTien;

    private Integer trangThai;

    private String ghiChu;

    private Long nguoiTao;

    private LocalDateTime ngayCapNhat;

    private LocalDateTime thoiGianMo;

    private LocalDateTime thoiGianDong;

    private String tenTang;

    // Bổ sung
    private BigDecimal tienCocHoan;
    private BigDecimal tienCocBiMat;
    private HinhThucCoc hinhThucCoc;
    private BigDecimal tongTienMon;

    private BigDecimal tienSauGiam;

    private BigDecimal vatPercent;

    private BigDecimal tienConLai;

    private BigDecimal tongTienDaThanhToan;

    /* ===== RELATIONSHIP INFO ===== */

    private Long idKhachHang;
    private String tenKhachHang;

    private Long idNhanVien;
    private String tenNhanVien;

    private List<Long> listIdBan;
    private List<String> listTenBan;

    private Long idDatBan;
    private Integer hinhThucDat;

    private Long idPhieuGiamGia;
}
