package com.fourspicehotpot.backend.modules.auth.repository;

import com.fourspicehotpot.backend.modules.auth.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
}
