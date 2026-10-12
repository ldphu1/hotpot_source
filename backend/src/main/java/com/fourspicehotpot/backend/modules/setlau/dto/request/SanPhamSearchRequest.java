package com.fourspicehotpot.backend.modules.setlau.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SanPhamSearchRequest {
    private String keyword;

    private Integer idDanhMuc;

    private Boolean trangThaiXoa;

    private Integer minPrice;

    private Integer maxPrice;

    private String sortGia; // asc | desc

}
