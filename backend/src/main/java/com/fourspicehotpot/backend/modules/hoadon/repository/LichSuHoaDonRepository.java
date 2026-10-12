package com.fourspicehotpot.backend.modules.hoadon.repository;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Long> {

    //check in
    boolean existsByHoaDon_IdAndHanhDong(Long idHoaDon, String hanhDong);

    // lịch sử hóa đơn theo hóa đơn, mới nhất trước
    List<LichSuHoaDon> findByHoaDon_IdOrderByThoiGianDesc(Long idHoaDon);

    List<LichSuHoaDon> findByHoaDon(HoaDon hoaDon);
}
