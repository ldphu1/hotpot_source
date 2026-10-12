package com.fourspicehotpot.backend.modules.auth.repository;

import com.fourspicehotpot.backend.modules.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
