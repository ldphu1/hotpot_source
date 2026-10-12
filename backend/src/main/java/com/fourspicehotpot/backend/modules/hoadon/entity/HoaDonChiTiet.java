package com.fourspicehotpot.backend.modules.hoadon.entity;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "hoa_don_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonChiTiet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", nullable = false)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", nullable = false)
    private BigDecimal thanhTien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    /* ================= RELATIONSHIP ================= */

    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;


    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;
}
