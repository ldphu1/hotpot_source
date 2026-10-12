package com.fourspicehotpot.backend.modules.checkin.repository;

import com.fourspicehotpot.backend.modules.checkin.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ThanhToanRepository extends JpaRepository<ThanhToan, Long> {

    @Query("""
                select tt
                from ThanhToan tt
                join fetch tt.phuongThucThanhToan pt
                join fetch tt.hoaDon hd
                where hd.id = :idHoaDon
                order by tt.id desc
            """)
    List<ThanhToan> findByHoaDon(@Param("idHoaDon") Long idHoaDon);

    @Query("""
                select coalesce(sum(tt.tongTien), 0)
                from ThanhToan tt
                where tt.hoaDon.id = :idHoaDon
                  and tt.trangThai = 1
            """)
    BigDecimal tongDaThanhToan(@Param("idHoaDon") Long idHoaDon);

    Optional<ThanhToan> findByMaThanhToan(String maThanhToan);
}