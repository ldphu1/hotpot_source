package com.fourspicehotpot.backend.modules.hoadon.entity;

import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hoa_don_ban_an")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonBanAn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "id_hoa_don")
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_ban_an")
    private BanAn banAn;

    @Column(name = "so_khach")
    private Integer soKhach;
}
