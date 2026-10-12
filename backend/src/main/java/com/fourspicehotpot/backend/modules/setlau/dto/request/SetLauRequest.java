package com.fourspicehotpot.backend.modules.setlau.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SetLauRequest {
    private String tenSanPham;
    private BigDecimal gia;
    private Integer idNuocLau;
    private Integer idDanhMuc;
    private String moTa;

    private List<ThanhPhanRequest> thanhPhans;
}
