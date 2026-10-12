package com.fourspicehotpot.backend.modules.cuoctrochuyen.repository;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.CuocTroChuyenProjection;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.TrangThaiCuocTroChuyen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CuocTroChuyenRepository extends JpaRepository<CuocTroChuyen, Long>, JpaSpecificationExecutor<CuocTroChuyen> {

    @Query(value = """
            SELECT TOP (:limit)
                c.id AS id,
                c.id_khach_hang AS idKhachHang,
                kh.ten_khach_hang AS tenKhachHang,
                c.id_nhan_vien_xu_ly AS idNhanVienXuLy,
                nv.ten_nhan_vien AS tenNhanVienXuLy,
                c.ten_khach AS tenKhach,
                c.loai AS loai,
                c.trang_thai AS trangThai,
                c.thoi_gian_tao AS thoiGianTao,
                c.thoi_gian_cap_nhat AS thoiGianCapNhat,
                c.thoi_gian_dong AS thoiGianDong,
                tn.id AS idTinNhanCuoi,
                tn.noi_dung AS tinNhanCuoi,
                tn.loai_tin_nhan AS loaiTinNhan,
                tn.thoi_gian AS thoiGianTinNhanCuoi,
                ISNULL(unread.so_tin_chua_doc, 0) AS soTinChuaDoc
            FROM cuoc_tro_chuyen c
            LEFT JOIN khach_hang kh ON kh.id = c.id_khach_hang
            LEFT JOIN nhan_vien nv ON nv.id = c.id_nhan_vien_xu_ly
            OUTER APPLY (
                SELECT TOP 1 t.id, t.noi_dung, t.loai_tin_nhan, t.thoi_gian
                FROM tin_nhan t
                WHERE t.id_cuoc_tro_chuyen = c.id
                ORDER BY t.thoi_gian DESC
            ) tn
            OUTER APPLY (
                SELECT COUNT(*) AS so_tin_chua_doc
                FROM tin_nhan t2
                WHERE t2.id_cuoc_tro_chuyen = c.id
                AND t2.da_doc = 0
            ) unread
            WHERE (:tenKhachHang IS NULL OR kh.ten_khach_hang LIKE %:tenKhachHang%)
            AND (:cursor IS NULL OR ISNULL(tn.thoi_gian, c.thoi_gian_tao) < :cursor)
            ORDER BY ISNULL(tn.thoi_gian, c.thoi_gian_tao) DESC
            """, nativeQuery = true)
    List<CuocTroChuyenProjection> getDanhSachCuocTroChuyen(
            @Param("tenKhachHang") String tenKhachHang,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit
    );

    Optional<CuocTroChuyen> findByKhachHang_IdAndTrangThai(Long idKhachHang, TrangThaiCuocTroChuyen trangThai);

}
