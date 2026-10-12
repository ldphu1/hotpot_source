package com.fourspicehotpot.backend.modules.khachhang.repository;

import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChiKhachHang, Long> {
    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
                update DiaChiKhachHang d
                set d.macDinh = false
                where d.khachHang.id = :khachHangId
            """)
    void clearMacDinhByKhachHang(@Param("khachHangId") Long khachHangId);

    List<DiaChiKhachHang> findByKhachHangId(Long khachHangId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DiaChiKhachHang d WHERE d.khachHang.id = :id")
    void deleteByKhachHangId(Long id);

}
