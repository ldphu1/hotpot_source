package com.fourspicehotpot.backend.modules.giohang.repository;

import com.fourspicehotpot.backend.modules.giohang.entity.GioHang;
import com.fourspicehotpot.backend.modules.giohang.entity.GioHangChiTiet;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Long> {
    Optional<GioHangChiTiet> findByGioHangAndSanPham(GioHang gioHang, SanPham sanPham);

    @Query("""
                SELECT COALESCE(SUM(ct.soLuong), 0)
                FROM GioHangChiTiet ct
                WHERE ct.gioHang = :gioHang
                AND LOWER(ct.sanPham.danhMuc.tenDanhMuc) IN :tenDanhMucs
            """)
    int sumSoLuongByDanhMucIn(@Param("gioHang") GioHang gioHang,
                              @Param("tenDanhMucs") List<String> tenDanhMucs);

    @Query(value = """
                SELECT COALESCE(SUM(ct.so_luong), 0)
                FROM gio_hang_chi_tiet ct
                JOIN san_pham sp ON ct.id_san_pham = sp.id
                WHERE ct.id_gio_hang = :gioHangId
                  AND sp.id_danh_muc = :danhMucId
            """, nativeQuery = true)
    int sumSoLuongByDanhMucId(
            @Param("gioHangId") Long gioHangId,
            @Param("danhMucId") Long danhMucId
    );
}
