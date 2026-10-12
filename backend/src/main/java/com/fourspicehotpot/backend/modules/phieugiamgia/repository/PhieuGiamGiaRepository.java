package com.fourspicehotpot.backend.modules.phieugiamgia.repository;

import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer>,
        JpaSpecificationExecutor<PhieuGiamGia> {

    Optional<PhieuGiamGia> findByMaGiamGiaIgnoreCase(String maGiamGia);

    @org.springframework.data.jpa.repository.Query("""
            SELECT p FROM PhieuGiamGia p 
            WHERE p.trangThaiXoa = false 
              AND :now BETWEEN p.ngayBatDau AND p.ngayKetThuc
            """)
    java.util.List<PhieuGiamGia> findActiveCoupons(@org.springframework.data.repository.query.Param("now") java.time.LocalDate now);

}
