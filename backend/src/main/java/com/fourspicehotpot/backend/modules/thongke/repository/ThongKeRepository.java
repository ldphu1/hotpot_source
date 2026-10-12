package com.fourspicehotpot.backend.modules.thongke.repository;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDon, Long> {

    /// ///////// HÔM NAY //////////////////////
    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= CAST(GETDATE() AS DATE)
                  AND ls.thoi_gian < DATEADD(DAY, 1, CAST(GETDATE() AS DATE))
            """, nativeQuery = true)
    BigDecimal getDoanhThuThucThuHomNay();

    @Query(value = """
            SELECT
            ISNULL(
                (
                    SELECT SUM(ls.so_tien)
                    FROM lich_su_thanh_toan ls
                    JOIN hoa_don hd2 ON hd2.id = ls.id_hoa_don
                    WHERE ls.trang_thai = 1
                      AND ls.loai_thanh_toan IN (1,2,4)
                      AND hd2.trang_thai IN (8,9)
                      AND ls.thoi_gian >= CAST(GETDATE() AS DATE)
                      AND ls.thoi_gian < DATEADD(DAY,1,CAST(GETDATE() AS DATE))
                )
            ,0)
            +
            ISNULL(
                (
                    SELECT SUM(ISNULL(hd.tong_tien_mon,0))
                    FROM hoa_don hd
                    WHERE hd.trang_thai IN (0,1,2,3,4,5,6,7)
                      AND hd.thoi_gian_tao >= CAST(GETDATE() AS DATE)
                      AND hd.thoi_gian_tao < DATEADD(DAY,1,CAST(GETDATE() AS DATE))
                )
            ,0)
            """, nativeQuery = true)
    BigDecimal getDoanhThuDuKienHomNay();

    ///  ///////////////// HÔM QUA //////////////////
    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEADD(DAY, -1, CAST(GETDATE() AS DATE))
                  AND ls.thoi_gian < CAST(GETDATE() AS DATE)
            """, nativeQuery = true)
    BigDecimal getDoanhThuThucThuHomQua();

    /// ///////////////////////////////////////////////
    /// TUẦN ////
    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
                  AND ls.thoi_gian < DATEADD(DAY, 8 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
            """, nativeQuery = true)
    BigDecimal getDoanhThuThucThuTuan();

    @Query(value = """
            SELECT
            ISNULL(
                (
                    SELECT SUM(ls.so_tien)
                    FROM lich_su_thanh_toan ls
                    JOIN hoa_don hd2 ON hd2.id = ls.id_hoa_don
                    WHERE ls.trang_thai = 1
                      AND ls.loai_thanh_toan IN (1,2,4)
                      AND hd2.trang_thai IN (8,9)
                      AND ls.thoi_gian >= DATEADD(DAY,1-DATEPART(WEEKDAY,GETDATE()),CAST(GETDATE() AS DATE))
                      AND ls.thoi_gian < DATEADD(DAY,8-DATEPART(WEEKDAY,GETDATE()),CAST(GETDATE() AS DATE))
                )
            ,0)
            +
            ISNULL(
                (
                    SELECT SUM(ISNULL(hd.tong_tien_mon,0))
                    FROM hoa_don hd
                    WHERE hd.trang_thai IN (0,1,2,3,4,5,6,7)
                      AND hd.thoi_gian_tao >= DATEADD(DAY,1-DATEPART(WEEKDAY,GETDATE()),CAST(GETDATE() AS DATE))
                      AND hd.thoi_gian_tao < DATEADD(DAY,8-DATEPART(WEEKDAY,GETDATE()),CAST(GETDATE() AS DATE))
                )
            ,0)
            """, nativeQuery = true)
    BigDecimal getDoanhThuDuKienTuan();

    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEADD(WEEK, -1, DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE)))
                  AND ls.thoi_gian < DATEADD(DAY, 1 - DATEPART(WEEKDAY, GETDATE()), CAST(GETDATE() AS DATE))
            """, nativeQuery = true)
    BigDecimal getDoanhThuTuanTruoc();


    ///  /////////////////// THÁNG /////////////////////
    // Không được suawr
    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
                  AND ls.thoi_gian < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
            """, nativeQuery = true)
    BigDecimal getDoanhThuThucThuThang();

    @Query(value = """
            SELECT
            ISNULL(
                (
                    SELECT SUM(ls.so_tien)
                    FROM lich_su_thanh_toan ls
                    JOIN hoa_don hd2 ON hd2.id = ls.id_hoa_don
                    WHERE ls.trang_thai = 1
                      AND ls.loai_thanh_toan IN (1,2,4)
                      AND hd2.trang_thai IN (8,9)
                      AND ls.thoi_gian >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
                      AND ls.thoi_gian < DATEADD(MONTH,1,
                            DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                )
            ,0)
            +
            ISNULL(
                (
                    SELECT SUM(ISNULL(hd.tong_tien_mon,0))
                    FROM hoa_don hd
                    WHERE hd.trang_thai IN (0,1,2,3,4,5,6,7)
                      AND hd.thoi_gian_tao >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
                      AND hd.thoi_gian_tao < DATEADD(MONTH,1,
                            DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                )
            ,0)
            """, nativeQuery = true)
    BigDecimal getDoanhThuDuKienThang();


    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEADD(MONTH, -1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))
                  AND ls.thoi_gian < DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1)
            """, nativeQuery = true)
    BigDecimal getDoanhThuThangTruoc();


    /// ///////////////////// NĂM ////////
    // Không được suawr
    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEFROMPARTS(YEAR(GETDATE()), 1, 1)
                  AND ls.thoi_gian < DATEADD(YEAR, 1, DATEFROMPARTS(YEAR(GETDATE()), 1, 1))
            """, nativeQuery = true)
    BigDecimal getDoanhThuThucThuNam();

    @Query(value = """
            SELECT
            ISNULL(
                (
                    SELECT SUM(ls.so_tien)
                    FROM lich_su_thanh_toan ls
                    JOIN hoa_don hd2 ON hd2.id = ls.id_hoa_don
                    WHERE ls.trang_thai = 1
                      AND ls.loai_thanh_toan IN (1,2,4)
                      AND hd2.trang_thai IN (8,9)
                      AND ls.thoi_gian >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
                      AND ls.thoi_gian < DATEADD(YEAR,1,
                            DATEFROMPARTS(YEAR(GETDATE()),1,1))
                )
            ,0)
            +
            ISNULL(
                (
                    SELECT SUM(ISNULL(hd.tong_tien_mon,0))
                    FROM hoa_don hd
                    WHERE hd.trang_thai IN (0,1,2,3,4,5,6,7)
                      AND hd.thoi_gian_tao >= DATEFROMPARTS(YEAR(GETDATE()),1,1)
                      AND hd.thoi_gian_tao < DATEADD(YEAR,1,
                            DATEFROMPARTS(YEAR(GETDATE()),1,1))
                )
            ,0)
            """, nativeQuery = true)
    BigDecimal getDoanhThuDuKienNam();


    @Query(value = """
                SELECT ISNULL(SUM(ls.so_tien), 0)
                FROM lich_su_thanh_toan ls
                JOIN hoa_don hd ON hd.id = ls.id_hoa_don
                WHERE ls.trang_thai = 1
                  AND ls.loai_thanh_toan IN (1,2,4)
                  AND hd.trang_thai IN (8,9)
                  AND ls.thoi_gian >= DATEFROMPARTS(YEAR(GETDATE()) - 1, 1, 1)
                  AND ls.thoi_gian < DATEFROMPARTS(YEAR(GETDATE()), 1, 1)
            """, nativeQuery = true)
    BigDecimal getDoanhThuNamTruoc();

    @Query(value = """
                SELECT
                    COUNT(CASE WHEN trang_thai in (8,9) THEN 1 END),
            
                    ISNULL(AVG(CASE WHEN trang_thai in (8,9) THEN tien_con_lai END), 0),
            
                    ISNULL(SUM(CASE WHEN trang_thai in (8,9) THEN tien_coc END), 0),
            
                    ISNULL(SUM(CASE WHEN trang_thai in (8,9) THEN so_tien_duoc_giam END), 0),
            
                    COUNT(CASE WHEN trang_thai = -1 THEN 1 END),
            
                    ISNULL(SUM(CASE WHEN trang_thai in (8,9) THEN tien_con_lai END), 0)
            
                FROM hoa_don
                WHERE thoi_gian_thanh_toan BETWEEN :from AND :to
            """, nativeQuery = true)
    Object[] thongKeTongQuan(LocalDateTime from, LocalDateTime to);

    @Query(value = """
                WITH thang AS (
                    SELECT 1 AS thang UNION ALL
                    SELECT 2 UNION ALL
                    SELECT 3 UNION ALL
                    SELECT 4 UNION ALL
                    SELECT 5 UNION ALL
                    SELECT 6 UNION ALL
                    SELECT 7 UNION ALL
                    SELECT 8 UNION ALL
                    SELECT 9 UNION ALL
                    SELECT 10 UNION ALL
                    SELECT 11 UNION ALL
                    SELECT 12
                )
                SELECT
                    t.thang,
                    ISNULL(SUM(hd.tong_tien_mon), 0) AS doanhThu
                FROM thang t
                LEFT JOIN hoa_don hd
                    ON MONTH(hd.thoi_gian_thanh_toan) = t.thang
                    AND YEAR(hd.thoi_gian_thanh_toan) = YEAR(GETDATE())
                    AND hd.trang_thai IN (8,9)
                GROUP BY t.thang
                ORDER BY t.thang
            """, nativeQuery = true)
    List<Object[]> thongKeDoanhThuTheoThang();

    @Query(value = """
                SELECT TOP 5
                    sp.ten_san_pham,
                    sp.gia,
                    sp.hinh_anh,
                    SUM(hdct.so_luong) AS daBan
                FROM hoa_don_chi_tiet hdct
                JOIN hoa_don hd ON hd.id = hdct.id_hoa_don
                JOIN san_pham sp ON sp.id = hdct.id_san_pham
                WHERE hd.trang_thai IN (8,9)
                  AND sp.la_set = 1
                GROUP BY
                    sp.id,
                    sp.ten_san_pham,
                    sp.gia,
                    sp.hinh_anh
                ORDER BY daBan DESC
            """, nativeQuery = true)
    List<Object[]> getTopSet();

    @Query(value = """
                SELECT 
                    COALESCE(SUM(CASE WHEN loai_hoa_don = 2 THEN 1 ELSE 0 END), 0),
                    COALESCE(SUM(CASE WHEN loai_hoa_don = 1 THEN 1 ELSE 0 END), 0)
                FROM hoa_don
                WHERE trang_thai in (8,9)
            """, nativeQuery = true)
    List<Object[]> getBookingChannel();


    @Query(value = """
            
                SELECT
                COALESCE(SUM(CASE WHEN hd.trang_thai = 0 THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai IN (1,2) THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai IN (3,4) THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai IN (5,6) THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai = 7 THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai = 8 THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai = 9 THEN 1 ELSE 0 END),0),
                COALESCE(SUM(CASE WHEN hd.trang_thai = -1 THEN 1 ELSE 0 END),0)
            FROM hoa_don hd
            """, nativeQuery = true)
    List<Object[]> thongKeTrangThai();
}
