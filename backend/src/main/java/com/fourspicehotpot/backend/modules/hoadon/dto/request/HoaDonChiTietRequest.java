package com.fourspicehotpot.backend.modules.hoadon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HoaDonChiTietRequest {

    private Long idHoaDon;

    private Long idSanPham;

    private Integer soLuong;

    private BigDecimal donGia;

    private BigDecimal thanhTien;

    private Integer trangThai;

    private Long idBanAn;
}
