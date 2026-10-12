package com.fourspicehotpot.backend.modules.checkin.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "phuong_thuc_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhuongThucThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phuong_thuc", length = 50)
    private String maPhuongThuc;

    @Column(name = "ten_phuong_thuc", nullable = false, length = 100)
    private String tenPhuongThuc;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
