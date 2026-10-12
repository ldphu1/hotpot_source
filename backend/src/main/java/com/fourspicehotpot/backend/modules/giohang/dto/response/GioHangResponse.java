package com.fourspicehotpot.backend.modules.giohang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GioHangResponse {

    private Long id;
    private Long idKhachHang;

    private List<GioHangChiTietResponse> items;

    private BigDecimal tongTien;
}