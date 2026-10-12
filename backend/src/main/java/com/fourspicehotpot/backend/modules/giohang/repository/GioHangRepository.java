package com.fourspicehotpot.backend.modules.giohang.repository;

import com.fourspicehotpot.backend.modules.giohang.entity.GioHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Long> {
    Optional<GioHang> findByKhachHang(KhachHang khachHang);

    Optional<GioHang> findBySessionId(String sessionId);
}
