package com.fourspicehotpot.backend.modules.datban.entity;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "dat_ban")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatBan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // FK khách hàng
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    // FK bàn ăn
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "dat_ban_ban_an",
            joinColumns = @JoinColumn(name = "id_dat_ban"),
            inverseJoinColumns = @JoinColumn(name = "id_ban_an")
    )
    private List<BanAn> banAns;

    @Column(name = "ma_dat_ban", length = 50)
    private String maDatBan;

    @Column(name = "thoi_gian_dat", nullable = false)
    private LocalDateTime thoiGianDat;

    @Column(name = "so_nguoi_toi_da", nullable = false)
    private Integer soNguoiToiDa;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "hinh_thuc_dat", nullable = false)
    private Integer hinhThucDat;

    @Column(name = "trang_thai", length = 20)
    private String trangThai;

    @CreationTimestamp
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @UpdateTimestamp
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    // Bổ sung không được sửa
    @Column(name = "tien_coc")
    private BigDecimal tienCoc;

    @Column(name = "phan_tram_coc")
    private BigDecimal phanTramCoc;

    @Column(name = "han_giu_ban")
    private LocalDateTime hanGiuBan;

    @Column(name = "han_thanh_toan_coc")
    private LocalDateTime hanThanhToanCoc;

    @Column(name = "trang_thai_coc")
    private String trangThaiCoc;

    @Column(name = "thoi_gian_ket_thuc_du_kien")
    private LocalDateTime thoiGianKetThucDuKien;
}