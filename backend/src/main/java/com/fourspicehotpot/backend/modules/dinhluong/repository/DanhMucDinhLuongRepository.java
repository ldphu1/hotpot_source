package com.fourspicehotpot.backend.modules.dinhluong.repository;

import com.fourspicehotpot.backend.modules.dinhluong.entity.DanhMucDinhLuong;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DanhMucDinhLuongRepository extends JpaRepository<DanhMucDinhLuong, Integer> {

    @Query("""
             SELECT DISTINCT dv.id
             FROM DanhMucDinhLuong dmdl
             JOIN dmdl.dinhLuong dl
             JOIN dl.donVi dv
             WHERE dmdl.danhMuc.id = :idDanhMuc
            """)
    List<Integer> getDonViIdsByDanhMuc(@Param("idDanhMuc") Integer idDanhMuc);

    @Modifying
    @Query("""
            DELETE FROM DanhMucDinhLuong d
            WHERE d.danhMuc.id = :idDanhMuc
            """)
    void deleteByDanhMucId(Integer idDanhMuc);

    void deleteByDinhLuongIn(List<DinhLuong> dinhLuongs);
}
