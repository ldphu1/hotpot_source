package com.fourspicehotpot.backend.modules.auth.repository;

import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaChiCustomerRepository extends JpaRepository<DiaChiKhachHang, Long> {

    List<DiaChiKhachHang> findByKhachHangId(Long khachHangId);

    DiaChiKhachHang findByKhachHangIdAndMacDinhTrue(Long khachHangId);
}
