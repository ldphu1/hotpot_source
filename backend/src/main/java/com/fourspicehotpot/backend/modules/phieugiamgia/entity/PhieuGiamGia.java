package com.fourspicehotpot.backend.modules.phieugiamgia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_giam_gia", length = 50)
    private String maGiamGia;

    @Column(name = "ten_giam_gia", nullable = false, length = 255)
    private String tenGiamGia;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "kieu_giam_gia", nullable = false)
    private Integer kieuGiamGia;

    @Column(name = "phuong_thuc_giam_gia", nullable = false)
    private Integer phuongThucGiamGia;

    @Column(name = "dieu_kien_su_dung", precision = 18, scale = 2)
    private BigDecimal dieuKienSuDung;

    @Column(name = "gia_tri_giam", nullable = false, precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "tao_boi")
    private Long taoBoi;

    @Column(name = "cap_nhat_boi")
    private Long capNhatBoi;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa = false;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDate ngayKetThuc;

    @Column(name = "trang_thai")
    private Integer trangThai;

}
