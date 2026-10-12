package com.fourspicehotpot.backend.modules.checkin.entity;


import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Hóa đơn
    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    // 🔗 Phương thức thanh toán
    @ManyToOne
    @JoinColumn(name = "phuong_thuc_thanh_toan_id", nullable = false)
    private PhuongThucThanhToan phuongThucThanhToan;

    @Column(name = "ma_thanh_toan", length = 50)
    private String maThanhToan;

    @Column(name = "tong_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    // Tự set thời gian khi insert/update
    @PrePersist
    public void prePersist() {
        ngayThanhToan = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}