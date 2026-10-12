package com.fourspicehotpot.backend.modules.nhanvien.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "nhan_vien")
@Builder
public class NhanVien {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tai_khoan")
    private String taiKhoan;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "ma_nhan_vien")
    private String maNhanVien;

    @Column(name = "ten_nhan_vien")
    private String tenNhanVien;

    @Column(name = "anh_nhan_vien")
    private String anhNhanVien;

    @Column(name = "dia_chi_chi_tiet")
    private String diaChiChiTiet;

    @Column(name = "tinh_thanh")
    private String tinhThanh;

    @Column(name = "xa_phuong")
    private String xaPhuong;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "email")
    private String email;

    @Column(name = "can_cuoc_cong_dan")
    private String canCuocCongDan;

    @Column(name = "chuc_vu")
    private Integer chucVu;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "tao_boi")
    private Long taoBoi;

    @Column(name = "cap_nhat_boi")
    private Long capNhatBoi;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa;

}
