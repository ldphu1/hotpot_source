package com.fourspicehotpot.backend.modules.setlau.repository;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Long>, JpaSpecificationExecutor<SanPham> {


    @Query("""
            select sp from SanPham sp where sp.danhMuc.id = :idDanhMuc
            order by sp.id desc      
            """)
    List<SanPham> findSanPhamByDanhMuc(@Param("idDanhMuc") Integer idDanhMuc);

    @Query("""
                SELECT sp
                FROM SanPham sp
                WHERE sp.laSet = true
                            ORDER BY sp.id desc 
            """)
    List<SanPham> getAllSetLau();


    @Query("""
            SELECT sp
            FROM SanPham sp
            WHERE sp.laSet = true
            AND (:keyword IS NULL OR sp.tenSanPham LIKE %:keyword%)
            AND (:idNuocLau IS NULL OR sp.nuocLau.id = :idNuocLau)
            AND (:trangThaiXoa IS NULL OR sp.trangThaiXoa = :trangThaiXoa)
            AND (:minPrice IS NULL OR sp.gia >= :minPrice)
            AND (:maxPrice IS NULL OR sp.gia <= :maxPrice)
            """)
    List<SanPham> searchSetLau(
            String keyword,
            Long idNuocLau,
            Boolean trangThaiXoa,
            Integer minPrice,
            Integer maxPrice
    );

    List<SanPham> findByLaSetFalseAndTrangThaiXoaFalse();

    Optional<SanPham> findByIdAndLaSetTrue(Integer id);


    //check in

    @Query("""
                select sp
                from SanPham sp
                where sp.laSet = false
                  and sp.trangThaiXoa = false
                order by sp.id desc
            """)
    List<SanPham> findAllMonLe();

    @Query("""
                select sp
                from SanPham sp
                where sp.laSet = true
                  and sp.trangThaiXoa = false
                order by sp.id desc
            """)
    List<SanPham> findAllSetLau();

    @Query("""
                select sp
                from SanPham sp
                where sp.laSet = false
                  and sp.trangThaiXoa = false
                  and (:keyword is null or lower(sp.tenSanPham) like lower(concat('%', :keyword, '%')))
                  and (:idDanhMuc is null or sp.danhMuc.id = :idDanhMuc)
                  and (:giaMin is null or sp.gia >= :giaMin)
                  and (:giaMax is null or sp.gia <= :giaMax)
                order by sp.id desc
            """)
    List<SanPham> searchMonLe(String keyword, Long idDanhMuc, BigDecimal giaMin, BigDecimal giaMax);

    @Query("""
                select sp
                from SanPham sp
                where sp.laSet = true
                  and sp.trangThaiXoa = false
                  and (:keyword is null or lower(sp.tenSanPham) like lower(concat('%', :keyword, '%')))
                  and (:idNuocLau is null or sp.nuocLau.id = :idNuocLau)
                order by sp.id desc
            """)
    List<SanPham> searchSetLau(String keyword, Long idNuocLau);

    List<SanPham> findByLaSetTrueAndTrangThaiXoaFalseOrderByIdDesc();

    @Query(value = """
            SELECT
                sp.id,
                sp.ten_san_pham,
                sp.gia,
                sp.la_set,
                dgg_best.gia_tri_giam,
                dgg_best.so_tien_giam_thuc_te,
                sp.hinh_anh,
                dm.ten_danh_muc
            FROM san_pham sp
            LEFT JOIN danh_muc dm
                ON sp.id_danh_muc = dm.id
            
            OUTER APPLY (
                SELECT TOP 1
                    dgg.gia_tri_giam,
                    (sp.gia * dgg.gia_tri_giam / 100.0) AS so_tien_giam_thuc_te
                FROM dot_giam_gia_san_pham ct
                JOIN dot_giam_gia dgg
                    ON ct.dot_giam_gia_id = dgg.id
                WHERE ct.san_pham_id = sp.id
                  AND DATEADD(HOUR, 7, GETDATE())
                        BETWEEN dgg.ngay_bat_dau AND dgg.ngay_ket_thuc
                  AND dgg.active = 1
                  AND dgg.is_deleted = 0
                ORDER BY dgg.gia_tri_giam DESC
            ) dgg_best
            
            WHERE sp.trang_thai_xoa = 0
            """, nativeQuery = true)
    List<Object[]> getSanPhamClient();


    @Query(value = """
            SELECT
                sp.id,
                sp.ten_san_pham,
                sp.gia,
                sp.la_set,
                dgg_best.gia_tri_giam,
                dgg_best.so_tien_giam_thuc_te,
                sp.id_danh_muc,
                sp.hinh_anh,
                dm.ten_danh_muc
            FROM san_pham sp
            LEFT JOIN danh_muc dm
                ON sp.id_danh_muc = dm.id
            
            OUTER APPLY (
                SELECT TOP 1
                    dgg.gia_tri_giam,
                    (sp.gia * dgg.gia_tri_giam / 100.0) AS so_tien_giam_thuc_te
                FROM dot_giam_gia_san_pham ct
                JOIN dot_giam_gia dgg
                    ON ct.dot_giam_gia_id = dgg.id
                WHERE ct.san_pham_id = sp.id
                  AND DATEADD(HOUR, 7, GETDATE())
                        BETWEEN dgg.ngay_bat_dau AND dgg.ngay_ket_thuc
                  AND dgg.active = 1
                  AND dgg.is_deleted = 0
                ORDER BY dgg.gia_tri_giam DESC
            ) dgg_best
            
            WHERE sp.trang_thai_xoa = 0
              AND (:idDanhMuc IS NULL OR sp.id_danh_muc = :idDanhMuc)
            """, nativeQuery = true)
    List<Object[]> getSanPhamClientByDanhMuc(@Param("idDanhMuc") Integer idDanhMuc);

    @Query(value = """
            SELECT
                sp.id,
                sp.ten_san_pham,
                sp.gia,
                sp.la_set,
                dgg_best.gia_tri_giam,
                dgg_best.so_tien_giam_thuc_te,
                sp.hinh_anh,
                dm.ten_danh_muc
            FROM san_pham sp
            LEFT JOIN danh_muc dm
                ON sp.id_danh_muc = dm.id
            
            OUTER APPLY (
                SELECT TOP 1
                    dgg.gia_tri_giam,
                    (sp.gia * dgg.gia_tri_giam / 100.0) AS so_tien_giam_thuc_te
                FROM dot_giam_gia_san_pham ct
                JOIN dot_giam_gia dgg
                    ON ct.dot_giam_gia_id = dgg.id
                WHERE ct.san_pham_id = sp.id
                  AND DATEADD(HOUR, 7, GETDATE())
                        BETWEEN dgg.ngay_bat_dau AND dgg.ngay_ket_thuc
                  AND dgg.active = 1
                  AND dgg.is_deleted = 0
                ORDER BY dgg.gia_tri_giam DESC
            ) dgg_best
            
            WHERE sp.trang_thai_xoa = 0
              AND sp.la_set = :laSet
            """, nativeQuery = true)
    List<Object[]> getSanPhamClientByLoai(@Param("laSet") Boolean laSet);
}
