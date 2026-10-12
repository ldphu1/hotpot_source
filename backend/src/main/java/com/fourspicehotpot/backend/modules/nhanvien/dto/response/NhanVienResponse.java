package com.fourspicehotpot.backend.modules.nhanvien.dto.response;

import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Builder
@Data
public class NhanVienResponse {

    private Long id;

    private String tenNhanVien;
    private LocalDate ngaySinh;
    private Boolean gioiTinh;
    private String soDienThoai;
    private String email;
    private Integer chucVu;
    private String tinhThanh;
    private String xaPhuong;
    private String diaChiChiTiet;
    private String anhNhanVien;
    private Boolean trangThaiXoa;

    public static NhanVienResponse fromEntity(NhanVien nv) {
        return NhanVienResponse.builder()
                .id(nv.getId())
                .tenNhanVien(nv.getTenNhanVien())
                .ngaySinh(nv.getNgaySinh())
                .gioiTinh(nv.getGioiTinh())
                .soDienThoai(nv.getSoDienThoai())
                .email(nv.getEmail())
                .chucVu(nv.getChucVu())
                .tinhThanh(nv.getTinhThanh())
                .xaPhuong(nv.getXaPhuong())
                .diaChiChiTiet(nv.getDiaChiChiTiet())
                .anhNhanVien(nv.getAnhNhanVien())
                .trangThaiXoa(nv.getTrangThaiXoa())
                .build();
    }
}


