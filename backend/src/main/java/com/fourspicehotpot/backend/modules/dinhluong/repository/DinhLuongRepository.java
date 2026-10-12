package com.fourspicehotpot.backend.modules.dinhluong.repository;

import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse;
import com.fourspicehotpot.backend.modules.dinhluong.entity.DinhLuong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DinhLuongRepository extends JpaRepository<DinhLuong, Long> {

    @Query("""
             SELECT new com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse(
                 dl.id,
                 dl.giaTri,
                 dv.tenDonVi
             )
             FROM DanhMucDinhLuong dmdl
             JOIN dmdl.dinhLuong dl
             JOIN dl.donVi dv
             WHERE dmdl.danhMuc.id = :idDanhMuc
            """)
    List<DinhLuongResponse> getByDanhMuc(@Param("idDanhMuc") Integer idDanhMuc);

    @Query("""
             SELECT dl
             FROM DinhLuong dl
             WHERE dl.donVi.id IN :donViIds
            """)
    List<DinhLuong> findByDonViIds(@Param("donViIds") List<Long> donViIds);

    List<DinhLuong> findByDonViId(Long donViId);
}
