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
public class NuocLauResponse {
    private Long id;
    private String maNuocLau;
    private String tenNuocLau;
    private String moTa;
    private BigDecimal giaPhuThu;
    private String hinhAnh;
    private Boolean trangThaiXoa;

}
