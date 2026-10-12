package com.fourspicehotpot.backend.modules.checkin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonThanhToanItemResponse {
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal giaGoc;
    private BigDecimal giaSauGiam;
    private BigDecimal thanhTien;
    private Integer trangThai;

}
