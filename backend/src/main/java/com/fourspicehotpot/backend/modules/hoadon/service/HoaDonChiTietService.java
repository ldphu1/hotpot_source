package com.fourspicehotpot.backend.modules.hoadon.service;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonDetailResponse;

public interface HoaDonChiTietService {
    HoaDonChiTiet themMon(Long idHoaDon, Long idSanPham, Integer soLuong);

    HoaDonChiTiet tangSoLuong(Long idHoaDonChiTiet);

    HoaDonChiTiet giamSoLuong(Long idHoaDonChiTiet);

    void xoaMon(Long idHoaDonChiTiet);

    HoaDonDetailResponse getDetail(Long id);
}
