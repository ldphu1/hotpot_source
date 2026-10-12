package com.fourspicehotpot.backend.modules.dotgiamgia.dto.response;

import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DotGiamGiaResponse {

    private Long id;
    private String maDot;
    private String tenDot;
    private BigDecimal giaTriGiam;


    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;

    private List<Long> sanPhamIds;

    private Integer trangThai;

    // ✅ UI hiển thị (nếu BE tính)
    private Integer trangThaiHienThi;
    private Integer maxDiscountPercent;
    // ✅ THÊM CÁI NÀY
    private Boolean active;

    private List<SanPhamResponse> sanPhams;
}
