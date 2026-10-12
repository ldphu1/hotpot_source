package com.fourspicehotpot.backend.modules.dotgiamgia.repository;

import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DotGiamGiaSanPhamRepository extends JpaRepository<DotGiamGiaSanPham, Long> {

    // Lấy mapping theo đợt giảm giá
    List<DotGiamGiaSanPham> findByDotGiamGia_Id(Long dotGiamGiaId);

    // Xóa mapping
    @Modifying
    @Transactional
    void deleteByDotGiamGia_Id(Long dotGiamGiaId);

    // Check trùng (optional nhưng nên có)
    boolean existsByDotGiamGia_IdAndSanPham_Id(Long dotGiamGiaId, Long sanPhamId);

    // Lấy theo sản phẩm (optional)
    List<DotGiamGiaSanPham> findBySanPham_Id(Long sanPhamId);

    @Query("""
                SELECT dgsp
                FROM DotGiamGiaSanPham dgsp
                JOIN FETCH dgsp.dotGiamGia dgg
                WHERE dgsp.sanPham.id = :sanPhamId
                  AND dgg.active = true
                  AND dgg.ngayBatDau <= :now
                  AND dgg.ngayKetThuc >= :now
            """)
    List<DotGiamGiaSanPham> findDangApDung(
            @Param("sanPhamId") Long sanPhamId,
            @Param("now") LocalDateTime now
    );

    @Query("""
                SELECT dgsp
                FROM DotGiamGiaSanPham dgsp
                JOIN FETCH dgsp.dotGiamGia dgg
                JOIN FETCH dgsp.sanPham sp
                WHERE dgg.active = true 
                  AND dgg.isDeleted = 0
                  AND :now BETWEEN dgg.ngayBatDau AND dgg.ngayKetThuc
                  AND sp.trangThai = true 
                  AND sp.trangThaiXoa = false
            """)
    List<DotGiamGiaSanPham> findAllActiveDiscountedProducts(@Param("now") LocalDateTime now);
}
