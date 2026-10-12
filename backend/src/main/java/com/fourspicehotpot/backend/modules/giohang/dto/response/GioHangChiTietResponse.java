package com.fourspicehotpot.backend.modules.giohang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GioHangChiTietResponse {

    private Long id;

    private Long idSanPham;
    private String tenSanPham;
    private BigDecimal gia;
    private String hinhAnh;

    private Integer soLuong;

    private BigDecimal thanhTien;
}