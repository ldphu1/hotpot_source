package com.fourspicehotpot.backend.modules.checkin.service;

import com.fourspicehotpot.backend.modules.checkin.dto.request.TaoThanhToanRequest;
import com.fourspicehotpot.backend.modules.checkin.dto.response.HoaDonThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.TaoThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.dto.response.ThanhToanResponse;
import com.fourspicehotpot.backend.modules.checkin.entity.PhuongThucThanhToan;

import java.util.List;

public interface ThanhToanService {

    List<PhuongThucThanhToan> getAllPhuongThucThanhToan();

    TaoThanhToanResponse thanhToanTienMat(TaoThanhToanRequest request);

    TaoThanhToanResponse thanhToanChuyenKhoan(TaoThanhToanRequest request);

    List<ThanhToanResponse> getDanhSachThanhToanTheoHoaDon(Long idHoaDon);

    HoaDonThanhToanResponse getChiTietHoaDonThanhToan(Long idHoaDon);

    void xacNhanThanhToanThanhCong(String maThanhToan);

    void xacNhanHoanTatHoaDon(Long idHoaDon);

}