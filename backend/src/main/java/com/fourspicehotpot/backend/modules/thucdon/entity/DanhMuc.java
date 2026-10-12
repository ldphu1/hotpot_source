package com.fourspicehotpot.backend.modules.thucdon.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "danh_muc")
@Builder
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "ma_danh_muc")
    private String maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(name = "ten_danh_muc", unique = true)
    private String tenDanhMuc;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa;

}
