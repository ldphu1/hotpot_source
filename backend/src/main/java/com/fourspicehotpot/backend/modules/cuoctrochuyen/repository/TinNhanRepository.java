package com.fourspicehotpot.backend.modules.cuoctrochuyen.repository;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.TinNhanProjection;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TinNhanRepository extends JpaRepository<TinNhan, Long>, JpaSpecificationExecutor<TinNhan> {

    @Query(value = """
            
            SELECT
                                tn.id AS idTinNhan,
                                tn.noi_dung AS noiDung,
                                tn.loai_tin_nhan AS loaiTinNhan,
                                tn.da_doc AS daDoc,
                                tn.thoi_gian AS thoiGian,
                                tn.loai_nguoi_gui AS loaiNguoiGui,
                                kh.ten_khach_hang AS tenKhachHang,
                                nv.ten_nhan_vien AS tenNhanVien,
                                tdk.duong_dan AS duongDanFile
                            FROM tin_nhan tn
            JOIN cuoc_tro_chuyen c ON c.id = tn.id_cuoc_tro_chuyen
            LEFT JOIN khach_hang kh ON kh.id = c.id_khach_hang
            LEFT JOIN nhan_vien nv ON nv.id = tn.id_nguoi_gui
            AND tn.id_nguoi_gui != c.id_khach_hang
            LEFT JOIN tep_dinh_kem tdk ON tdk.id_tin_nhan = tn.id
            WHERE tn.id_cuoc_tro_chuyen = :idCuocTroChuyen
            AND (:cursor IS NULL OR tn.thoi_gian < :cursor)
            ORDER BY tn.thoi_gian DESC
            OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
            """, nativeQuery = true)
    List<TinNhanProjection> getTinNhanTheoCuocTroChuyen(
            @Param("idCuocTroChuyen") Long idCuocTroChuyen,
            @Param("cursor") LocalDateTime cursor,
            @Param("limit") int limit
    );
}
