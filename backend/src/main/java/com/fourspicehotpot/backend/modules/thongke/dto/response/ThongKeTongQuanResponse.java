package com.fourspicehotpot.backend.modules.thongke.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ThongKeTongQuanResponse {
    private Long tongHoaDon;
    private BigDecimal trungBinhDon;
    private BigDecimal tienCoc;
    private BigDecimal giamGia;
    private Long huyDon;
    private BigDecimal thucNhan;
}
