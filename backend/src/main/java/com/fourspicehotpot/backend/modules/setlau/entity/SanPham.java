package com.fourspicehotpot.backend.modules.setlau.entity;

import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_san_pham")
    private String maSanPham;

    @Column(name = "ten_san_pham", nullable = false)
    private String tenSanPham;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal gia;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "la_set")
    private Boolean laSet = false;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    // ===== Quan hệ =====

    @ManyToOne
    @JoinColumn(name = "id_danh_muc", nullable = false)
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_nuoc_lau")
    private NuocLau nuocLau;

    @OneToMany(mappedBy = "set", cascade = CascadeType.ALL)
    @lombok.ToString.Exclude
    private List<SetThanhPhan> lstThanhPhan;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_dinh_luong", nullable = true)
    private DinhLuong dinhLuong;

    // ===== Lifecycle =====

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}