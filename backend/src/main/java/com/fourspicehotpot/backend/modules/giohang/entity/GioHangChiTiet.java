package com.fourspicehotpot.backend.modules.giohang.entity;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "gio_hang_chi_tiet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gio_hang", nullable = false)
    private GioHang gioHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "gia", nullable = false, precision = 12, scale = 2)
    private BigDecimal gia;
}