package com.fourspicehotpot.backend.modules.hoadon.repository;

import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Long> {

    @Query("""
                select ls
                from LichSuThanhToan ls
                where ls.hoaDon.id = :idHoaDon
                order by ls.thoiGian desc
            """)
    List<LichSuThanhToan> findByHoaDon(@Param("idHoaDon") Long idHoaDon);
}
