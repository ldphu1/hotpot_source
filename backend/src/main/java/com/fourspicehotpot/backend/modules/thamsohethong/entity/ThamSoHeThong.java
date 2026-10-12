package com.fourspicehotpot.backend.modules.thamsohethong.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tham_so_he_thong")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThamSoHeThong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_tham_so", nullable = false, unique = true, length = 100)
    private String maThamSo;

    @Column(name = "ten_tham_so", length = 255)
    private String tenThamSo;

    @Column(name = "gia_tri", nullable = false, length = 255)
    private String giaTri;

    @Column(name = "kieu_du_lieu", nullable = false, length = 50)
    private String kieuDuLieu;

    @Column(name = "nhom", length = 100)
    private String nhom;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai = true;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @PrePersist
    public void prePersist() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}