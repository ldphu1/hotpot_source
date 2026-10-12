package com.fourspicehotpot.backend.modules.setlau.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThanhPhanResponse {
    private Long idSanPham;
    private String tenSanPham;
    private BigDecimal gia;
    private Integer soLuong;

    private BigDecimal giaTri;
    private String tenDonVi;

    private String tenDanhMuc;
    private String hinhAnh;

}
