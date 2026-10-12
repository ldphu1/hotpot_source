package com.fourspicehotpot.backend.modules.hoadon.service;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;

public interface LichSuHoaDonService {

    void saveLichSuHoaDon(HoaDon hoaDon, String hanhDong, Integer trangThai, String ghiChu);
}
