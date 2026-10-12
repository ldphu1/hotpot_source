package com.fourspicehotpot.backend.modules.hoadon.service;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuDonHangResponse;

import java.util.List;

public interface CustomerXemLichSuHoaDonService {

    List<LichSuDonHangResponse> getLichSu(String email);

    CustomerHoaDonDetailResponse getChiTiet(Long idHoaDon, String email);

    LichSuDonHangResponse getLichSuByEmailAndMaHoaDon(String email, String maHoaDon);
}
