package com.fourspicehotpot.backend.modules.hoadon.entity;

import com.fourspicehotpot.backend.common.enums.LoaiThanhToan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_thanh_toan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuThanhToan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "ma_giao_dich")
    private String maGiaoDich;
    @Column(name = "so_tien")
    private BigDecimal soTien;
    @Column(name = "phuong_thuc")
    private String phuongThuc;
    @Column(name = "trang_thai")
    private Integer trangThai;
    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;
    @Column(name = "ghi_chu")
    private String ghiChu;
    @Column(name = "loai_thanh_toan")
    private LoaiThanhToan loaiThanhToan;
    @ManyToOne
    @JoinColumn(name = "id_hoa_don", referencedColumnName = "id")
    private HoaDon hoaDon;
}
