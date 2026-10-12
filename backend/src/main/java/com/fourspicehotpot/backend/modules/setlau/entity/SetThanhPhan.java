package com.fourspicehotpot.backend.modules.setlau.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "set_thanh_phan")
public class SetThanhPhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "so_luong")
    private Integer soLuong = 1;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa = false;

    // ===== Quan hệ =====

    // Set (chính là 1 sản phẩm có laSet = true)
    @ManyToOne
    @JoinColumn(name = "id_set", nullable = false)
    @JsonIgnore
    @lombok.ToString.Exclude
    private SanPham set;

    @ManyToOne
    @JoinColumn(name = "id_san_pham_thanh_phan", nullable = false)
    @lombok.ToString.Exclude
    private SanPham sanPhamThanhPhan;
}