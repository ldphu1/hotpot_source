package com.fourspicehotpot.backend.modules.dinhluong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DinhLuongResponse {

    private Integer id;
    private String maDinhLuong;
    private BigDecimal giaTri;

    private Long idDonVi;
    private String tenDonVi;
    private Boolean trangThaiXoa;

    public DinhLuongResponse(Integer id, BigDecimal giaTri, String tenDonVi) {
        this.id = id;
        this.giaTri = giaTri;
        this.tenDonVi = tenDonVi;
    }
}