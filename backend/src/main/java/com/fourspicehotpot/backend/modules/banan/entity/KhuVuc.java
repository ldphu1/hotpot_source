package com.fourspicehotpot.backend.modules.banan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "khu_vuc")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KhuVuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khu_vuc")
    private String maKhuVuc;

    @Column(name = "ten_khu_vuc")
    private String tenKhuVuc;

    @Column(name = "vi_tri")
    private String viTri;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
