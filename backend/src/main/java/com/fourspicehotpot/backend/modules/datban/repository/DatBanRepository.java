package com.fourspicehotpot.backend.modules.datban.repository;

import com.fourspicehotpot.backend.modules.checkin.dto.request.SearchKhachChoCheckInRequest;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DatBanRepository extends JpaRepository<DatBan, Long>, JpaSpecificationExecutor<DatBan> {

    @Query("""
                select distinct db
                from DatBan db
                left join fetch db.banAns ba
                left join fetch ba.khuVuc kv
                left join fetch db.khachHang kh
                order by db.id desc
            """)
    List<DatBan> findAllWithBan();


    @Query("SELECT d FROM DatBan d WHERE CAST(d.thoiGianDat AS date) = CAST(:date AS date) ORDER BY d.thoiGianDat ASC")
    List<DatBan> findByDate(@Param("date") LocalDateTime date);


    @Query("""
                select distinct db
                from DatBan db
                left join fetch db.banAns ba
                left join fetch ba.khuVuc kv
                left join fetch db.khachHang kh
                where CAST(db.thoiGianDat AS date) >= :#{#request.ngayFrom}
                  and CAST(db.thoiGianDat AS date) <= :#{#request.ngayTo}
                order by db.thoiGianDat asc
            """)
    List<DatBan> findAllByNgay(
          SearchKhachChoCheckInRequest request
    );

    DatBan findTopByBanAns_IdAndTrangThai(Long banId, String trangThai);

    // check in
    @Query("""
                select db
                from DatBan db
                where db.id = :idDatBan
            """)
    Optional<DatBan> findDatBanById(Long idDatBan);


    // check in
    @Query("""
                select db
                from DatBan db
                left join fetch db.banAns ba
                left join fetch ba.khuVuc kv
                left join fetch db.khachHang kh
                where db.trangThai in ('CHO_XAC_NHAN', 'DA_XAC_NHAN')
                  and not exists (
                      select 1
                      from HoaDon hd
                      where hd.datBan.id = db.id
                        and hd.trangThai = 0
                  )
                order by db.thoiGianDat asc
            """)
    List<DatBan> findAllKhachChoCheckIn();

    @Query("""
                select db
                from DatBan db
                left join fetch db.banAns ba
                left join fetch ba.khuVuc kv
                left join fetch db.khachHang kh
                where db.id = :id
            """)
    Optional<DatBan> findDetailById(@Param("id") Long id);


    Optional<DatBan> findDatBanById(Integer id);

    // Không được sưa
    @Query("""
                SELECT db FROM DatBan db
                LEFT JOIN FETCH db.banAns
                WHERE db.trangThaiCoc = :trangThaiCoc
                AND db.hanThanhToanCoc < :now
            """)
    List<DatBan> findDatBanCanHuy(
            @Param("trangThaiCoc") String trangThaiCoc,
            @Param("now") LocalDateTime now
    );


    /// /////////// BỔ SUNG CẤM SỬA .............
    @Query("""
                SELECT COUNT(db) > 0
                FROM DatBan db
                JOIN db.banAns b
                WHERE b.id = :idBan
                AND db.trangThai IN (
                 'CHO_XAC_NHAN',
                 'DA_XAC_NHAN',
                 'DA_COC',
                 'DA_CHECKIN',
                 'DANG_PHUC_VU'
                )
                AND (
                    db.thoiGianDat < :gioKetThucMoi
                    AND db.thoiGianKetThucDuKien > :gioBatDauMoi
                )
            """)
    boolean existsOverlap(
            @Param("idBan") Integer idBan,
            @Param("gioBatDauMoi") LocalDateTime gioBatDauMoi,
            @Param("gioKetThucMoi") LocalDateTime gioKetThucMoi

    );

    @Query("""
                SELECT COUNT(db) > 0
                FROM DatBan db
                JOIN db.banAns b
                WHERE b.id = :idBan
                AND db.id <> :idDatBan
                AND db.trangThai IN (
                 'CHO_XAC_NHAN',
                 'DA_XAC_NHAN',
                 'DA_COC',
                 'DA_CHECKIN',
                 'DANG_PHUC_VU'
                )
                AND (
                    db.thoiGianDat < :gioKetThucMoi
                    AND db.thoiGianKetThucDuKien > :gioBatDauMoi
                )
            """)
    boolean existsOverlapChinhMinh(
            @Param("idBan") Integer idBan,
            @Param(("idDatBan")) Integer idDatBan,
            @Param("gioBatDauMoi") LocalDateTime gioBatDauMoi,
            @Param("gioKetThucMoi") LocalDateTime gioKetThucMoi

    );


    @Query(value = """
            SELECT CASE 
                WHEN EXISTS (
                    SELECT 1
                    FROM dat_ban db
                    JOIN dat_ban_ban_an dbba 
                        ON db.id = dbba.id_dat_ban
                    WHERE dbba.id_ban_an = :idBan
                    AND db.trang_thai <> :trangThaiHuy
                    AND db.thoi_gian_dat <= :now
                    AND DATEADD(
                        MINUTE,
                        :thoiGianSuDung,
                        db.thoi_gian_dat
                    ) >= :now
                )
                THEN CAST(1 AS BIT)
                ELSE CAST(0 AS BIT)
            END
            """, nativeQuery = true)
    boolean existsDatBanDangSuDungBan(
            @Param("idBan") Integer idBan,
            @Param("now") LocalDateTime now,
            @Param("thoiGianSuDung") Integer thoiGianSuDung,
            @Param("trangThaiHuy") String trangThaiHuy
    );

    Integer id(Integer id);

    @Query("""
                select distinct db
                from DatBan db
                left join fetch db.banAns ba
                left join fetch ba.khuVuc kv
                left join fetch db.khachHang kh
                where (:keyword is null or :keyword = ''
                   or lower(db.maDatBan) like lower(concat('%', :keyword, '%'))
                   or lower(kh.tenKhachHang) like lower(concat('%', :keyword, '%'))
                   or lower(kh.soDienThoai) like lower(concat('%', :keyword, '%')))
                order by db.thoiGianDat desc
            """)
    List<DatBan> searchDatBanByKeyword(@Param("keyword") String keyword);
}
