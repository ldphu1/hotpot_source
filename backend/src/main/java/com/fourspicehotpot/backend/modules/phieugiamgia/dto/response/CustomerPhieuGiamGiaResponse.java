package com.fourspicehotpot.backend.modules.phieugiamgia.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerPhieuGiamGiaResponse {
    private String ma;
    private String ten;
    private BigDecimal giaTri;
}