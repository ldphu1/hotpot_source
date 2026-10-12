package com.fourspicehotpot.backend.modules.dotgiamgia.entity;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "dot_giam_gia_san_pham")
public class DotGiamGiaSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dot_giam_gia_id")
    private DotGiamGia dotGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;
     
}
