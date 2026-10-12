package com.fourspicehotpot.backend.modules.banan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ban_an")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BanAn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_ban_an")
    private String maBanAn;

    @Column(name = "ten_ban_an")
    private String tenBanAn;

    @Column(name = "suc_chua_toi_da")
    private Integer sucChuaToiDa;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @ManyToOne
    @JoinColumn(name = "id_khu_vuc", referencedColumnName = "id")
    private KhuVuc khuVuc;

    //28/03
    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (trangThai == null) trangThai = 1;
    }
}
