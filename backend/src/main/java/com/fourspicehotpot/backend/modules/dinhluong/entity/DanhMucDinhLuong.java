package com.fourspicehotpot.backend.modules.dinhluong.entity;

import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "danh_muc_dinh_luong")
@Builder
public class DanhMucDinhLuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "id_dinh_luong")
    private DinhLuong dinhLuong;
}
