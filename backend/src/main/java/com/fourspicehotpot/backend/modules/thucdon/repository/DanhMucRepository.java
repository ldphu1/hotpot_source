package com.fourspicehotpot.backend.modules.thucdon.repository;

import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer>, JpaSpecificationExecutor<DanhMuc> {

    @Query("""
             SELECT new com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse(
                 dm.id,
                 dm.maDanhMuc,
                 dm.tenDanhMuc,
                 dm.trangThaiXoa,
                 COUNT(sp.id)
             )
             FROM DanhMuc dm
             LEFT JOIN SanPham sp ON sp.danhMuc.id = dm.id
             GROUP BY dm.id, dm.maDanhMuc, dm.tenDanhMuc, dm.trangThaiXoa
             ORDER BY dm.id DESC
            """)
    List<DanhMucResponse> findAllDanhMucHoatDong();

    @Query(value = """
                SELECT * FROM danh_muc
                WHERE trang_thai_xoa = 0
                AND ten_danh_muc NOT IN (N'Lẩu', N'Nước lẩu')
                ORDER BY ten_danh_muc ASC
            """, nativeQuery = true)
    List<DanhMuc> findDanhMucMonLe();

    boolean existsByTenDanhMuc(String tenDanhMuc);

    boolean existsByTenDanhMucAndIdNot(String tenDanhMuc, Integer id);

    @Query("""
            SELECT new com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse(
                dm.id,
                dm.maDanhMuc,
                dm.tenDanhMuc,
                dm.trangThaiXoa,
                COUNT(sp.id)
            )
            FROM DanhMuc dm
            LEFT JOIN SanPham sp ON sp.danhMuc.id = dm.id
            WHERE
                (:keyword IS NULL OR LOWER(dm.tenDanhMuc) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:trangThaiXoa IS NULL OR dm.trangThaiXoa = :trangThaiXoa)
            AND (:idDanhMuc IS NULL OR dm.id = :idDanhMuc)
            GROUP BY dm.id, dm.maDanhMuc, dm.tenDanhMuc, dm.trangThaiXoa
            ORDER BY dm.id DESC
            """)
    List<DanhMucResponse> searchDanhMuc(
            String keyword,
            Boolean trangThaiXoa,
            Integer idDanhMuc
    );


}
