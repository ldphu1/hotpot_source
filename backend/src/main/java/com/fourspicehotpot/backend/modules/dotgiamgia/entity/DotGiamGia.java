package com.fourspicehotpot.backend.modules.dotgiamgia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_dot")
    private String maDot;

    @Column(name = "ten_dot")
    private String tenDot;

    @Column(name = "gia_tri_giam", nullable = false)
    private BigDecimal giaTriGiam;

    @Column(name = "ngay_bat_dau", nullable = false)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc", nullable = false)
    private LocalDateTime ngayKetThuc;

    @Column(name = "created_at")
    private LocalDateTime ngayTao;

    @Column(name = "is_deleted")
    private Integer isDeleted = 0; //o là chưa xóa

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "active")
    private Boolean active = true;


}
