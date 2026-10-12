package com.fourspicehotpot.backend.modules.dinhluong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "don_vi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonVi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", insertable = false, updatable = false)
    private Long id;

    @Column(name = "ma_don_vi", unique = true, insertable = false, updatable = false)
    private String maDonVi;

    @Column(name = "ten_don_vi", nullable = false)
    private String tenDonVi;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa = false;

    @OneToMany(mappedBy = "donVi")
    private List<DinhLuong> dinhLuongs;
}