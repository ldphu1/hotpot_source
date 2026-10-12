package com.fourspicehotpot.backend.modules.khachhang.dto.response;

import lombok.Data;

@Data
public class DiaChiKhachHangResponse {

    private Long id;
    private String tinhThanh;
    private String xaPhuong;
    private String diaChiChiTiet;
    private Boolean macDinh;
    private String hoTenNguoiNhan;
    private String sdtNguoiNhan;
}
