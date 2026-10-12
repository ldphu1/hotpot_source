package com.fourspicehotpot.backend.modules.thucdon.repository;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamCustomerRepository extends JpaRepository<SanPham, Long>, JpaSpecificationExecutor<SanPham> {
}
