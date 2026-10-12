package com.fourspicehotpot.backend.modules.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamGiamGiaAppResponse {
    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaGoc;
    private BigDecimal giaSauGiam;
    private Boolean laSet;
    private String tenDotGiamGia;
    private BigDecimal giaTriGiamCuaDot; // Giữ lại giá trị giảm
}
