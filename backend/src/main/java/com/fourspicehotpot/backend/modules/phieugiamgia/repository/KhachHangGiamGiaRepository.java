package com.fourspicehotpot.backend.modules.phieugiamgia.repository;

import com.fourspicehotpot.backend.modules.phieugiamgia.entity.KhachHangGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KhachHangGiamGiaRepository extends JpaRepository<KhachHangGiamGia, Long> {
    List<KhachHangGiamGia> findByPhieuGiamGia(PhieuGiamGia phieuGiamGia);

    void deleteByPhieuGiamGia(PhieuGiamGia phieuGiamGia);

    @Modifying
    @Query("""
            DELETE FROM KhachHangGiamGia k
            WHERE k.phieuGiamGia.id = :phieuId
            AND k.khachHang.id IN :ids
            """)
    void deleteByPhieuGiamGiaAndKhachHangIdIn(Integer phieuId, List<Long> ids);

    @Query("""
            SELECT DISTINCT k
            FROM KhachHangGiamGia k
            JOIN FETCH k.phieuGiamGia p
            WHERE k.khachHang.id = :idKhachHang
              AND (k.trangThaiXoa IS NULL OR k.trangThaiXoa = false)
            ORDER BY k.ngayTao DESC
            """)
    List<KhachHangGiamGia> findAllByKhachHangId(Long idKhachHang);
}
