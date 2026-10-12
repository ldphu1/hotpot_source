package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerHoaDonChiTietResponse {
    private Long id;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
    private Long idSanPham;
    private String tenSanPham;
    private String tenDanhMuc;
    private String hinhAnh;
    private Long idHoaDon;
    private String maHoaDon;
}
