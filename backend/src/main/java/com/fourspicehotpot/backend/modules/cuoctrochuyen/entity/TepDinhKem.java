package com.fourspicehotpot.backend.modules.cuoctrochuyen.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tep_dinh_kem")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TepDinhKem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tin_nhan", nullable = false)
    private TinNhan tinNhan;

    @Column(name = "duong_dan", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String duongDan;
}