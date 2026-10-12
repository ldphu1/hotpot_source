package com.fourspicehotpot.backend.modules.cuoctrochuyen.entity;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tin_nhan_san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinNhanSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tin_nhan", nullable = false)
    private TinNhan tinNhan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;
}