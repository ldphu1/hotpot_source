package com.fourspicehotpot.backend.modules.cuoctrochuyen.entity;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.TrangThaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuoc_tro_chuyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuocTroChuyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = true)
    private KhachHang khachHang;

    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_xu_ly")
    private NhanVien nhanVienXuLy;

    @Column(name = "ten_khach", length = 255)
    private String tenKhach;

    @Column(name = "loai", length = 50)
    private String loai;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", length = 50)
    private TrangThaiCuocTroChuyen trangThai;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;

    @Column(name = "thoi_gian_cap_nhat")
    private LocalDateTime thoiGianCapNhat;

    @Column(name = "thoi_gian_dong")
    private LocalDateTime thoiGianDong;

    // =========================
    @OneToMany(mappedBy = "cuocTroChuyen",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<TinNhan> danhSachTinNhan = new ArrayList<>();

    @OneToMany(mappedBy = "cuocTroChuyen",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<LichSuPhanCong> lichSuPhanCongs = new ArrayList<>();

    // =========================
    @PrePersist
    public void prePersist() {
        this.thoiGianTao = LocalDateTime.now();
        this.thoiGianCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.thoiGianCapNhat = LocalDateTime.now();
    }
}
