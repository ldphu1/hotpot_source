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
public class HoaDonDangDungTheoBanResponse {

    private Long idHoaDon;
    private String maHoaDon;
    private Integer trangThaiHoaDon;

    private Integer idBanAn;
    private String maBanAn;
    private String tenBanAn;
    private Integer khuVucId;
    private String tenKhuVuc;

    private Integer idDatBan;
    private String maDatBan;
    private Integer soNguoi;

    private String tenKhachHang;
    private String soDienThoai;

    private BigDecimal tongTienMon;
    private BigDecimal tongTien;
    private BigDecimal tienConLai;

    private BigDecimal tongTienDaThanhToan;

    private LocalDateTime thoiGianTao;
    private LocalDateTime thoiGianMo;

    private List<String> listTenBan;

    private Integer soNguoiDat;
}