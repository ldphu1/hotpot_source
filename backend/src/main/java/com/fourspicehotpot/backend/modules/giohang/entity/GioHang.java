package com.fourspicehotpot.backend.modules.giohang.entity;

import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gio_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GioHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_khach_hang")
    private KhachHang khachHang;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "thoi_gian_tao")
    private LocalDateTime thoiGianTao;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "gioHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GioHangChiTiet> chiTietList = new ArrayList<>();
}