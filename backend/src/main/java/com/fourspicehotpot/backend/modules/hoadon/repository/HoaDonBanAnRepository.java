package com.fourspicehotpot.backend.modules.hoadon.repository;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonBanAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoaDonBanAnRepository extends JpaRepository<HoaDonBanAn, Long> {

    @Query("""
                select distinct hdba
                from HoaDonBanAn hdba
                join fetch hdba.hoaDon hd
                join fetch hdba.banAn ba
                where ba.id = :idBanAn
                  and hd.trangThai in :trangThais
                order by hd.thoiGianTao desc
            """)
    List<HoaDonBanAn> findHoaDonDangHoatDongByBan(
            @Param("idBanAn") Integer idBanAn,
            @Param("trangThais") List<Integer> trangThais
    );

    @Query("""
                select distinct hdba
                from HoaDonBanAn hdba
                join fetch hdba.hoaDon hd
                join fetch hdba.banAn ba
                where hd.id = :idHoaDon
            """)
    List<HoaDonBanAn> findByHoaDonId(@Param("idHoaDon") Long idHoaDon);

    @Query("""
                select count(hdba) > 0
                from HoaDonBanAn hdba
                where hdba.hoaDon.id = :idHoaDon
                  and hdba.banAn.id = :idBanAn
            """)
    boolean existsByHoaDon_IdAndBanAn_Id(@Param("idHoaDon") Long idHoaDon,
                                         @Param("idBanAn") Long idBanAn);

    void deleteByHoaDon_Id(Long idHoaDon);

    @Query("""
                select distinct hdba
                from HoaDonBanAn hdba
                join fetch hdba.hoaDon hd
                join fetch hdba.banAn ba
                where hd.datBan.id = :idDatBan
            """)
    List<HoaDonBanAn> findByDatBanId(@Param("idDatBan") Integer idDatBan);

    @Query("""
                select distinct hdba
                from HoaDonBanAn hdba
                join fetch hdba.hoaDon hd
                join fetch hdba.banAn ba
                where ba.id = :idBanAn
                  and hd.trangThai = :trangThai
                order by hd.thoiGianTao desc
            """)
    List<HoaDonBanAn> findByBanAnAndTrangThai(
            @Param("idBanAn") Integer idBanAn,
            @Param("trangThai") Integer trangThai
    );

    @Query("""
                select distinct hdba.banAn.id
                from HoaDonBanAn hdba
                where hdba.hoaDon.id = :idHoaDon
            """)
    List<Integer> findAllBanIdsByHoaDonId(@Param("idHoaDon") Long idHoaDon);

    default Optional<HoaDonBanAn> findTopHoaDonDangHoatDongTheoBan(Integer idBanAn) {
        List<HoaDonBanAn> list = findHoaDonDangHoatDongByBan(
                idBanAn,
                List.of(1, 2, 3, 4, 5, 6)
        );
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }


    // đổi bàn vs ghép bàn
    @Query("""
                select hdba
                from HoaDonBanAn hdba
                join fetch hdba.banAn b
                where hdba.hoaDon.id = :idHoaDon
            """)
    List<HoaDonBanAn> findByHoaDon_Id(@Param("idHoaDon") Long idHoaDon);

    @Query("""
                select hdba
                from HoaDonBanAn hdba
                where hdba.hoaDon.id = :idHoaDon
                  and hdba.banAn.id = :idBanAn
            """)
    Optional<HoaDonBanAn> findByHoaDon_IdAndBanAn_Id(
            @Param("idHoaDon") Long idHoaDon,
            @Param("idBanAn") Long idBanAn
    );

    @Modifying
    @Query("""
                delete from HoaDonBanAn hdba
                where hdba.hoaDon.id = :idHoaDon
                  and hdba.banAn.id = :idBanAn
            """)
    void deleteByHoaDon_IdAndBanAn_Id(@Param("idHoaDon") Long idHoaDon,
                                      @Param("idBanAn") Long idBanAn);


    boolean existsByHoaDon_IdAndBanAn_Id(Integer idHoaDonGhep, Integer idBanGhep);

    @Query("""
                select coalesce(sum(db.soNguoiToiDa), 0)
                from HoaDonBanAn hdba
                join hdba.hoaDon hd
                join hd.datBan db
                where hdba.banAn.id = :idBanAn
                  and hd.trangThai in :trangThais
            """)
    Integer tongSoKhachDangDungTheoBan(
            @Param("idBanAn") Integer idBanAn,
            @Param("trangThais") List<Integer> trangThais
    );
}