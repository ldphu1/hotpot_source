package com.fourspicehotpot.backend.modules.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "role_permissions")
public class RolePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "permission_id")
    private Long permissionId;


}
