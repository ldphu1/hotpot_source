package com.fourspicehotpot.backend.modules.hoadon.repository;

import com.fourspicehotpot.backend.modules.hoadon.mapper.LichSuHoaDonProjection;
import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Long>, JpaSpecificationExecutor<HoaDon> {

    @Query("""
                SELECT DISTINCT h
                FROM HoaDon h
                JOIN h.banAns ba
                WHERE ba.id = :idBan
                  AND h.trangThai = 0
            """)
    Optional<HoaDon> findHoaDonDangMo(Long idBan);

    @Query("""
                SELECT DISTINCT h
                FROM HoaDon h
                JOIN h.banAns ba
                WHERE ba.id = :idBan
                  AND h.trangThai = :trangThai
            """)
    Optional<HoaDon> findByBanAn_IdAndTrangThai(Long idBan, Integer trangThai);

    // checkin
    @Query("""
                select count(hd) > 0
                from HoaDon hd
                where hd.datBan.id = :idDatBan
                  and hd.trangThai = 0
            """)
    boolean existsHoaDonDangMoByDatBan(Long idDatBan);

    // Kooong được sửa
    @Query("""
            SELECT DISTINCT hd FROM HoaDon hd
            LEFT JOIN FETCH hd.khachHang
            LEFT JOIN FETCH hd.nhanVien
            LEFT JOIN FETCH hd.banAns
            LEFT JOIN FETCH hd.datBan
            LEFT JOIN FETCH hd.phieuGiamGia
            LEFT JOIN FETCH hd.hoaDonChiTiets ct
            LEFT JOIN FETCH ct.sanPham sp
            LEFT JOIN FETCH sp.danhMuc
            LEFT JOIN FETCH hd.lichSuHoaDons ls
            WHERE hd.id = :id
            """)
    Optional<HoaDon> findDetailById(Long id);

    // Không được sửa - Lãnh Minh Hiếu
    Optional<HoaDon> findByDatBan(DatBan datBan);

    Optional<HoaDon> findByDatBan_IdAndTrangThai(Integer datBanId, Integer trangThai);

    // Lãnh Minh Hiếu
    @Query("""
                SELECT hd FROM HoaDon hd
                LEFT JOIN FETCH hd.banAns
                LEFT JOIN FETCH hd.nhanVien
                LEFT JOIN FETCH hd.khachHang
                LEFT JOIN FETCH hd.datBan
                LEFT JOIN FETCH hd.phieuGiamGia
                ORDER BY hd.thoiGianTao DESC
            """)
    List<HoaDon> findAllHoaDon();

    // Không được sửa - Lãnh Minh Hiếu
    @Query("""
                select hd
                from HoaDon hd
                left join fetch hd.hoaDonChiTiets ct
                left join fetch ct.sanPham sp
                where hd.datBan.id = :idDatBan
            """)
    Optional<HoaDon> findByDatBanId(@Param("idDatBan") Integer idDatBan);


    @Query("""
                select hd
                from HoaDon hd
                where exists (
                    select 1
                    from HoaDonBanAn hdba
                    where hdba.hoaDon.id = hd.id
                      and hdba.banAn.id = :idBan
                )
                and hd.trangThai in (1, 2, 3, 4, 5, 6)
                order by hd.thoiGianTao desc
            """)
    List<HoaDon> findHoaDonDangHoatDongByBan(@Param("idBan") Integer idBan);

    // Cấm sửa
    @Query(value = """
                SELECT 
                    hd.id                AS idHoaDon,
                    hd.ma_hoa_don        AS maHoaDon,
                    hd.trang_thai        AS trangThai,
                    hd.tong_tien_mon         AS tongTien,
            
                    db.so_nguoi_toi_da   AS soNguoiToiDa,
                    db.ngay_tao          AS ngayTao,
                    db.tien_coc          AS tienCoc,
                    db.thoi_gian_dat     AS thoiGianDat,
            
                    STRING_AGG(ba.ten_ban_an, ', ') AS tenBanAn,
                    STRING_AGG(kv.ten_khu_vuc, ', ') AS tenKhuVuc
            
                FROM hoa_don hd
            
                LEFT JOIN dat_ban db 
                    ON db.id = hd.id_dat_ban
            
                LEFT JOIN dat_ban_ban_an dbba 
                    ON dbba.id_dat_ban = db.id
            
                LEFT JOIN ban_an ba 
                    ON ba.id = dbba.id_ban_an
            
                LEFT JOIN khu_vuc kv 
                    ON kv.id = ba.id_khu_vuc
            
                WHERE hd.id_khach_hang = :idKhachHang
            
                GROUP BY 
                    hd.id,
                    hd.ma_hoa_don,
                    hd.trang_thai,
                    hd.tong_tien_mon,
                    db.so_nguoi_toi_da,
                    db.thoi_gian_dat,
                    db.ngay_tao,
                    db.tien_coc
            
                ORDER BY db.ngay_tao DESC
            """, nativeQuery = true)
    List<LichSuHoaDonProjection> getLichSuDonHang(@Param("idKhachHang") Long idKhachHang);

    @Query(value = """
                SELECT 
                    hd.id                AS idHoaDon,
                    hd.ma_hoa_don        AS maHoaDon,
                    hd.trang_thai        AS trangThai,
                    hd.tong_tien_mon         AS tongTien,
            
                    db.so_nguoi_toi_da   AS soNguoiToiDa,
                    db.ngay_tao          AS ngayTao,
                    db.tien_coc          AS tienCoc,
                    db.thoi_gian_dat     AS thoiGianDat,
            
                    STRING_AGG(ba.ten_ban_an, ', ') AS tenBanAn,
                    STRING_AGG(kv.ten_khu_vuc, ', ') AS tenKhuVuc
            
                FROM hoa_don hd
            
                LEFT JOIN dat_ban db 
                    ON db.id = hd.id_dat_ban
            
                LEFT JOIN dat_ban_ban_an dbba 
                    ON dbba.id_dat_ban = db.id
            
                LEFT JOIN ban_an ba 
                    ON ba.id = dbba.id_ban_an
            
                LEFT JOIN khu_vuc kv 
                    ON kv.id = ba.id_khu_vuc
            
                WHERE hd.id_khach_hang = :idKhachHang and hd.ma_hoa_don like concat('%', :maHoaDon, '%')
            
                GROUP BY 
                    hd.id,
                    hd.ma_hoa_don,
                    hd.trang_thai,
                    hd.tong_tien_mon,
                    db.so_nguoi_toi_da,
                    db.thoi_gian_dat,
                    db.ngay_tao,
                    db.tien_coc
            
            """, nativeQuery = true)
    Optional<LichSuHoaDonProjection> getLichSuDonHangByMaHoaDon(@Param("idKhachHang") Long idKhachHang, @Param("maHoaDon") String maHoaDon);

    @Query("""
            SELECT COUNT(hd) > 0
            FROM HoaDon hd
            JOIN hd.banAns b
            WHERE b.id = :idBan
            AND hd.trangThai IN :trangThais
            """)
    boolean existsHoaDonDangSuDungBan(
            @Param("idBan") Integer idBan,
            @Param("trangThais") List<Integer> trangThais
    );


    Integer id(Long id);
}
