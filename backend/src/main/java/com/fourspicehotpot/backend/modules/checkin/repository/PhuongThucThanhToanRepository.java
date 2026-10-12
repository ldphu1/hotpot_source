package com.fourspicehotpot.backend.modules.checkin.repository;

import com.fourspicehotpot.backend.modules.checkin.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Long> {

    @Query("""
                select pt
                from PhuongThucThanhToan pt
                where pt.trangThai = 1
                order by pt.id asc
            """)
    List<PhuongThucThanhToan> findAllActive();
}
