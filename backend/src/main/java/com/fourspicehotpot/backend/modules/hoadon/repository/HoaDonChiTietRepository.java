package com.fourspicehotpot.backend.modules.hoadon.repository;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Long> {

    Optional<HoaDonChiTiet> findByHoaDon_IdAndSanPham_Id(Long idHoaDon, Long idSanPham);

    List<HoaDonChiTiet> findAllByHoaDon_Id(Long idHoaDon);

    List<HoaDonChiTiet> findAllByHoaDon_IdAndTrangThai(Long idHoaDon, Integer trangThai);

    boolean existsByHoaDon_IdAndTrangThai(Long idHoaDon, Integer trangThai);

    @Query("""
                select hdct
                from HoaDonChiTiet hdct
                left join fetch hdct.sanPham sp
                left join fetch sp.danhMuc
                left join fetch hdct.hoaDon hd
                where hd.id = :idHoaDon
                order by hdct.id asc
            """)
    List<HoaDonChiTiet> findMonDaChonByHoaDonId(@Param("idHoaDon") Long idHoaDon);

    @Query("""
                select hdct
                from HoaDonChiTiet hdct
                left join fetch hdct.sanPham sp
                left join fetch sp.danhMuc
                left join fetch hdct.hoaDon hd
                where hdct.id = :idHoaDonChiTiet
            """)
    Optional<HoaDonChiTiet> findChiTietById(@Param("idHoaDonChiTiet") Long idHoaDonChiTiet);

    @Modifying
    @Query("""
                update HoaDonChiTiet h
                set h.trangThai = 1,
                    h.ngayCapNhat = :now
                where h.id = :idHoaDonChiTiet
                  and (h.trangThai is null or h.trangThai = 0)
            """)
    int daLenMon(@Param("idHoaDonChiTiet") Long idHoaDonChiTiet,
                 @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
                update HoaDonChiTiet h
                set h.trangThai = 1,
                    h.ngayCapNhat = :now
                where h.hoaDon.id = :idHoaDon
                 and (h.trangThai is null or h.trangThai = 0)
            """)
    int daLenTatCa(@Param("idHoaDon") Long idHoaDon,
                   @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
                delete from HoaDonChiTiet h
                where h.id = :idHoaDonChiTiet
            """)
    int xoaMonKhoiHoaDon(@Param("idHoaDonChiTiet") Long idHoaDonChiTiet);


    // Không được sửa
    List<HoaDonChiTiet> findByHoaDon(HoaDon hoaDon);

    // Không được sửa
    @Query("""
                SELECT COALESCE(SUM(hdct.thanhTien), 0)
                FROM HoaDonChiTiet hdct
                WHERE hdct.hoaDon.id = :hoaDonId
            """)
    BigDecimal tinhTongTienByHoaDon(@Param("hoaDonId") Long hoaDonId);

    /// thêm món thử
    @Query("""
                SELECT h
                FROM HoaDonChiTiet h
                WHERE h.hoaDon.id = :idHoaDon
                  AND h.sanPham.id = :idSanPham
                  AND h.trangThai = :trangThai
            """)
    Optional<HoaDonChiTiet> findMonChoByHoaDonVaSanPham(
            @Param("idHoaDon") Long idHoaDon,
            @Param("idSanPham") Long idSanPham,
            @Param("trangThai") Integer trangThai
    );

    Optional<HoaDonChiTiet> findByHoaDon_IdAndSanPham_IdAndTrangThai(Long hoaDonId, Long sanPhamId, Integer trangThai);

    List<HoaDonChiTiet> findByHoaDonId(Long hoaDonId);
}
