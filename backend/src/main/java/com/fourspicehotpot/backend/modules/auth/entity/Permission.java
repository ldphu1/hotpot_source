package com.fourspicehotpot.backend.modules.auth.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permissions")
@AllArgsConstructor
@NoArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_permission")
    private String tenPermission;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "trang_thai")
    private Boolean trangThai;
}