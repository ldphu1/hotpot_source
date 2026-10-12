package com.fourspicehotpot.backend.modules.dinhluong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "dinh_luong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DinhLuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_dinh_luong", unique = true)
    private String maDinhLuong;

    @Column(name = "gia_tri", nullable = false)
    private BigDecimal giaTri;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa = false;

    @ManyToOne
    @JoinColumn(name = "id_don_vi", nullable = false)
    private DonVi donVi;

    @OneToMany(mappedBy = "dinhLuong")
    private List<DanhMucDinhLuong> danhMucDinhLuongs;

}