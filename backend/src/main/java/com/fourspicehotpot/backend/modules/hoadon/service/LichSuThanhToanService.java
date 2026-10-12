package com.fourspicehotpot.backend.modules.hoadon.service;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.common.enums.LoaiThanhToan;

import java.math.BigDecimal;

public interface LichSuThanhToanService {

    void save(HoaDon hoaDon,
              String phuongThuc,
              BigDecimal soTien,
              String ghiChu,
              LoaiThanhToan loaiThanhToan);
}
