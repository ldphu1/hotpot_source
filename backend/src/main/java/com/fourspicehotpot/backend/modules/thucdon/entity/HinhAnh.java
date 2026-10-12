package com.fourspicehotpot.backend.modules.thucdon.entity;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hinh_anh")
public class HinhAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "trang_thai_xoa")
    private Boolean trangThaiXoa;

    @PrePersist
    protected void onCreate() {
        if (trangThaiXoa == null) {
            trangThaiXoa = false;
        }
    }

    // getter / setter generate
}

