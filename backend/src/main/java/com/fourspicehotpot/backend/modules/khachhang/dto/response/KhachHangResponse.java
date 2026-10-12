package com.fourspicehotpot.backend.modules.khachhang.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class KhachHangResponse {
    private Long id;
    private String maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private Integer trangThai;
    private List<DiaChiKhachHangResponse> diaChiList;
    private String anhDaiDien;

    public KhachHangResponse() {
    }

    public KhachHangResponse(Long id, String tenKhachHang, String soDienThoai,
                             String email, LocalDate ngaySinh) {
        this.id = id;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.ngaySinh = ngaySinh;
    }
}

