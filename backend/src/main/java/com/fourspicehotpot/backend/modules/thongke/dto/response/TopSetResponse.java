package com.fourspicehotpot.backend.modules.thongke.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TopSetResponse {
    private String hinhAnh;
    private String ten;
    private BigDecimal gia;
    private Long ban;
}
