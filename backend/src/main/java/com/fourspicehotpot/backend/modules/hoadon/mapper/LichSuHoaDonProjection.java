package com.fourspicehotpot.backend.modules.hoadon.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface LichSuHoaDonProjection {
    Long getIdHoaDon();

    String getMaHoaDon();

    Integer getTrangThai();

    BigDecimal getTongTien();

    Integer getSoNguoiToiDa();

    LocalDateTime getNgayTao();

    BigDecimal getTienCoc();

    LocalDateTime getThoiGianDat();

    String getTenBanAn();

    String getTenKhuVuc();

}
