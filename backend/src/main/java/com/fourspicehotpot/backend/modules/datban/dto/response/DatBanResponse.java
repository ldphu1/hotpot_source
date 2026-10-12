package com.fourspicehotpot.backend.modules.datban.dto.response;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
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
public class DatBanResponse {

    private Integer id;

    private String maDatBan;

    private Long idKhachHang;
    private String maKhachHang;
    private String soDienThoai;
    private String tenKhuVuc;

    private String tenKhachHang;

    private List<Integer> listIdBan;
    private List<String> listTenBan;

    private LocalDateTime thoiGianDat;

    private Integer soNguoiToiDa;

    private String ghiChu;

    private Integer hinhThucDat;

    private String trangThai;

    private LocalDateTime ngayTao;

    private LocalDateTime ngayCapNhat;

    // Bổ sung không được sửa
    private BigDecimal tienCoc;
    private BigDecimal phanTramCoc;

    private LocalDateTime hanGiuBan;
    private LocalDateTime hanThanhToanCoc;

    private String trangThaiCoc;


    private HoaDonResponse hoaDon;
    private List<HoaDonChiTietResponse> chiTietHoaDons;
    private List<LichSuHoaDonResponse> lichSuHoaDons;
}