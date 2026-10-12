package com.fourspicehotpot.backend.modules.khachhang.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class KhachHangSearchRequest {

    private String hoTen;
    private String soDienThoai;
    private String email;
    private Integer trangThai;
    private LocalDate ngaySinh;
}

