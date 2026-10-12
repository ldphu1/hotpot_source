package com.fourspicehotpot.backend.modules.hoadon.entity;

import com.fourspicehotpot.backend.common.enums.HinhThucCoc;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "hoa_don")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ma_hoa_don")
    private String maHoaDon;
    @Column(name = "loai_hoa_don", nullable = false)
    private Integer loaiHoaDon;
    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;
    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan;
    @Column(name = "tien_coc")
    private BigDecimal tienCoc;
    @Column(name = "tien_thue")
    private BigDecimal tienThue;
    @Column(name = "ten_nguoi_nhan")
    private String tenNguoiNhan;
    @Column(name = "so_dien_thoai")
    private String soDienThoai;
    @Column(name = "dia_chi_giao")
    private String diaChiGiao;
    @Column(name = "phi_van_chuyen")
    private BigDecimal phiVanChuyen;
    @Column(name = "so_tien_duoc_giam")
    private BigDecimal soTienDuocGiam;
    @Builder.Default
    @Column(name = "tong_tien", nullable = false)
    private BigDecimal tongTien = BigDecimal.ZERO;
    @Builder.Default
    @Column(name = "trang_thai")
    private Integer trangThai = 0;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @Column(name = "nguoi_tao")
    private Long nguoiTao;
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    @Column(name = "thoi_gian_mo")
    private LocalDateTime thoiGianMo;
    @Column(name = "thoi_gian_dong")
    private LocalDateTime thoiGianDong;


    // Bổ sung
    @Column(name = "tien_coc_hoan")
    private BigDecimal tienCocHoan;

    @Column(name = "tien_coc_bi_mat")
    private BigDecimal tienCocBiMat;

    @Enumerated(EnumType.STRING)
    @Column(name = "hinh_thuc_coc")
    private HinhThucCoc hinhThucCoc;

    @Column(name = "tong_tien_mon")
    private BigDecimal tongTienMon;

    @Column(name = "tien_sau_giam")
    private BigDecimal tienSauGiam;

    @Column(name = "vat_percent")
    private BigDecimal vatPercent;

    @Column(name = "tien_con_lai")
    private BigDecimal tienConLai;

    @Column(name = "tong_tien_da_thanh_toan")
    private BigDecimal tongTienDaThanhToan;

    /* ================= RELATIONSHIP ================= */
    @OneToMany(mappedBy = "hoaDon")
    private Set<LichSuHoaDon> lichSuHoaDons;
    @OneToMany(mappedBy = "hoaDon")
    private Set<HoaDonChiTiet> hoaDonChiTiets;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    private PhieuGiamGia phieuGiamGia;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_dat_ban")
    private DatBan datBan;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hoa_don_ban_an",
            joinColumns = @JoinColumn(name = "id_hoa_don"),
            inverseJoinColumns = @JoinColumn(name = "id_ban_an")
    )
    private List<BanAn> banAns;

    @PrePersist
    public void prePersist() {
        if (thoiGianTao == null) {
            thoiGianTao = LocalDateTime.now();
        }
    }

}
