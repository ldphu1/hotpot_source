package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class TienCocPreviewResponse {
    private BigDecimal tienCocHoan;

    private BigDecimal tienCocBiMat;
}
