package com.fourspicehotpot.backend.modules.cuoctrochuyen.entity;

import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_phan_cong")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuPhanCong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuoc_tro_chuyen", nullable = false)
    private CuocTroChuyen cuocTroChuyen;

    // =========================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private NhanVien nhanVien;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @PrePersist
    public void prePersist() {
        if (this.thoiGian == null) {
            this.thoiGian = LocalDateTime.now();
        }
    }
}
