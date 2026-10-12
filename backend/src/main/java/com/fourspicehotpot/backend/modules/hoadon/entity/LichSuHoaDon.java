package com.fourspicehotpot.backend.modules.hoadon.entity;

import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_hoa_don")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class LichSuHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "hanh_dong", nullable = false, length = 100)

    private String hanhDong;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;


    // Bổ sung
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;
}