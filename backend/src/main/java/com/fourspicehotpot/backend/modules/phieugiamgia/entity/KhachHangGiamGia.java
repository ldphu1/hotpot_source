package com.fourspicehotpot.backend.modules.phieugiamgia.entity;

import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "khach_hang_giam_gia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "id_phieu_giam_gia", nullable = false)
    private PhieuGiamGia phieuGiamGia;

    @Column(name = "thoi_diem_giam_gia")
    private LocalDateTime thoiDiemGiamGia;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "email")
    private String email;

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