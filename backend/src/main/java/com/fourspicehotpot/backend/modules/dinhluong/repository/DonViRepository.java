package com.fourspicehotpot.backend.modules.dinhluong.repository;

import com.fourspicehotpot.backend.modules.dinhluong.entity.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonViRepository extends JpaRepository<DonVi, Long> {

    @Query("""
            SELECT DISTINCT dv
            FROM DonVi dv
            LEFT JOIN FETCH dv.dinhLuongs dl
            LEFT JOIN DanhMucDinhLuong dmdl ON dmdl.dinhLuong.id = dl.id
            LEFT JOIN DanhMuc dm ON dm.id = dmdl.danhMuc.id
                       order by dv.id desc 
            """)
    List<DonVi> getAllDonViWithDinhLuong();

    @Query("""
            SELECT DISTINCT dv
            FROM DonVi dv
            LEFT JOIN FETCH dv.dinhLuongs dl
            LEFT JOIN DanhMucDinhLuong dmdl ON dmdl.dinhLuong.id = dl.id
            LEFT JOIN DanhMuc dm ON dm.id = dmdl.danhMuc.id
            WHERE dv.id = :id
            """)
    Optional<DonVi> getDetailDonVi(@Param("id") Long id);


    @Query("""
            SELECT DISTINCT dv
            FROM DonVi dv
            LEFT JOIN FETCH dv.dinhLuongs dl
            LEFT JOIN DanhMucDinhLuong dmdl ON dmdl.dinhLuong.id = dl.id
            LEFT JOIN DanhMuc dm ON dm.id = dmdl.danhMuc.id
            WHERE (:keyword IS NULL OR LOWER(dv.tenDonVi) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:trangThaiXoa IS NULL OR dv.trangThaiXoa = :trangThaiXoa)
            ORDER BY dv.id DESC
            """)
    List<DonVi> searchDonVi(String keyword, Boolean trangThaiXoa);
}
