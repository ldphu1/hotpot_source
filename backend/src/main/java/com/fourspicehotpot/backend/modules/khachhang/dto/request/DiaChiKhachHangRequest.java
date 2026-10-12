package com.fourspicehotpot.backend.modules.khachhang.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiaChiKhachHangRequest {
    private Long id;

    @NotBlank(message = "Tỉnh / thành phố không được để trống")
    private String tinhThanh;

    @NotBlank(message = "Phường / xã không được để trống")
    private String xaPhuong;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String diaChiChiTiet;

    @NotNull(message = "Trạng thái mặc định không được để trống")
    private Boolean macDinh;

    private String hoTenNguoiNhan;
    private String sdtNguoiNhan;
}

