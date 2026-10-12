package com.fourspicehotpot.backend.modules.setlau.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "nuoc_lau")
public class NuocLau {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_nuoc_lau")
    private String maNuocLau;

    @Column(name = "ten_nuoc_lau", nullable = false)
    private String tenNuocLau;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "gia_phu_thu")
    private BigDecimal giaPhuThu;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @PrePersist
    protected void onCreate() {
        if (giaPhuThu == null) {
            giaPhuThu = BigDecimal.ZERO;
        }
        if (trangThaiXoa == null) {
            trangThaiXoa = false;
        }
    }

    // getter / setter generate
}
