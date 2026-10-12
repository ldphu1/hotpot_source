package com.fourspicehotpot.backend.modules.dotgiamgia.repository;

import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Long> {

    // ================== ACTIVE BY SAN PHAM ==================
    @Query("""
            SELECT d
            FROM DotGiamGia d
            JOIN DotGiamGiaSanPham m ON m.dotGiamGia.id = d.id
            WHERE m.sanPham.id = :sanPhamId
              AND d.isDeleted = 0
              AND d.active = true
              AND CURRENT_TIMESTAMP BETWEEN d.ngayBatDau AND d.ngayKetThuc
            """)
    List<DotGiamGia> findActiveBySanPham(@Param("sanPhamId") Long sanPhamId);
    
    // ================== GET ALL ==================
    List<DotGiamGia> findByIsDeleted(Integer isDeleted);

    // ================== CHECK OVERLAP ==================
    @Query("""
                SELECT COUNT(d.id)
                FROM DotGiamGiaSanPham m
                JOIN m.dotGiamGia d
                WHERE m.sanPham.id = :sanPhamId
                  AND d.active = true
                  AND d.isDeleted = 0
                  AND (:excludeId IS NULL OR d.id <> :excludeId)
                  AND (
                        d.ngayBatDau <= :end
                    AND d.ngayKetThuc >= :start
                  )
            """)
    int countOverlap(
            @Param("sanPhamId") Long sanPhamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") Long excludeId
    );


    // ================== SEARCH ==================
    @Query("""
                SELECT d FROM DotGiamGia d
                WHERE d.isDeleted = 0
                  AND (
                        :keyword IS NULL 
                        OR :keyword = '' 
                        OR LOWER(d.tenDot) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  )
            """)
    List<DotGiamGia> search(@Param("keyword") String keyword);

    //  Lấy đợt giảm giá vào APP
    @Query("""
            SELECT d FROM DotGiamGia d 
            WHERE d.isDeleted = 0 
              AND d.active = true 
              AND :now BETWEEN d.ngayBatDau AND d.ngayKetThuc
            """)
    List<DotGiamGia> findActiveCampaigns(@Param("now") LocalDateTime now);
}

