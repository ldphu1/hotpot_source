package com.fourspicehotpot.backend.modules.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotGiamGiaAppResponse {
    private Long id;
    private String maDot;
    private String tenDot;
    private BigDecimal giaTriGiam;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
}
