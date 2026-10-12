package com.fourspicehotpot.backend.modules.khachhang.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "dia_chi")
public class DiaChiKhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "dia_chi_cu_the")
    private String diaChiChiTiet;

    @Column(name = "thanh_pho")
    private String tinhThanh;

    @Column(name = "phuong_xa")
    private String xaPhuong;

    @Column(name = "quan_huyen")
    private String quanHuyen;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "mac_dinh")
    private Boolean macDinh;

    @Column(name = "ho_ten_nguoi_nhan")
    private String hoTenNguoiNhan;

    @Column(name = "sdt_nguoi_nhan")
    private String sdtNguoiNhan;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
}
