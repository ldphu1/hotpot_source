package com.fourspicehotpot.backend.modules.cuoctrochuyen.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tin_nhan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TinNhan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuoc_tro_chuyen", nullable = false)
    private CuocTroChuyen cuocTroChuyen;

    @Column(name = "id_nguoi_gui")
    private Long idNguoiGui;

    @Column(name = "loai_tin_nhan", length = 50)
    private String loaiTinNhan;

    @Column(name = "loai_nguoi_gui")
    private String loaiNguoiGui;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "da_doc")
    private Boolean daDoc;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    // =========================
    @OneToMany(mappedBy = "tinNhan",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<TepDinhKem> tepDinhKems = new ArrayList<>();

    @OneToMany(mappedBy = "tinNhan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TinNhanSanPham> tinNhanSanPhams = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        if (this.daDoc == null) {
            this.daDoc = false;
        }

        if (this.thoiGian == null) {
            this.thoiGian = LocalDateTime.now();
        }
    }
}