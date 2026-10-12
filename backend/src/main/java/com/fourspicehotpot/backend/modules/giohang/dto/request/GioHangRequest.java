package com.fourspicehotpot.backend.modules.giohang.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GioHangRequest {

    private Long idKhachHang;
    private Long idSanPham;
    private Integer soLuong;
}