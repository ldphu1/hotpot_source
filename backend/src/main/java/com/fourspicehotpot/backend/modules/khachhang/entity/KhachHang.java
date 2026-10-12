package com.fourspicehotpot.backend.modules.khachhang.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "khach_hang")
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_khach_hang")
    private String maKhachHang;

    @Column(name = "mat_khau")
    private String matKhau;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;


    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "email")
    private String email;

    @Column(name = "ngay_sinh")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate ngaySinh;

    @Column(name = "anh_dai_dien")
    private String anhDaiDien;

    @Column(name = "gioi_tinh")
    private Boolean gioiTinh;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai = 1;

    @OneToMany(mappedBy = "khachHang", fetch = FetchType.LAZY)
    private List<DiaChiKhachHang> diaChiList;

    // Bổ sung không được sửa
    @Column(name = "diem_tich_luy")
    private Double diemTichLuy;

    @Column(name = "tong_chi_tieu")
    private Double tongChiTieu;

    @Column(name = "hang")
    private String hang;

}
