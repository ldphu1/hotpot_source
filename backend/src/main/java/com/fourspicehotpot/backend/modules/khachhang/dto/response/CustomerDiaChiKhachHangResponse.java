package com.fourspicehotpot.backend.modules.khachhang.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDiaChiKhachHangResponse {

    private Long id;
    private String tinhThanh;
    private String xaPhuong;
    private String diaChiChiTiet;
    private Boolean macDinh;
    private String hoTenNguoiNhan;
    private String sdtNguoiNhan;
}
