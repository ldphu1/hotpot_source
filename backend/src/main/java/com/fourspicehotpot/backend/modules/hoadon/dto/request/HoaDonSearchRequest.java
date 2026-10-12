package com.fourspicehotpot.backend.modules.hoadon.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HoaDonSearchRequest {
    private String soDienThoai;
    private String tenKhachHang;
    private String trangThai;
    private String loaiHoaDon;
    private String tenKhuVuc;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
}
