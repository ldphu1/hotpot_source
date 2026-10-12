package com.fourspicehotpot.backend.modules.hoadon.service;

import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.checkin.dto.request.CheckInRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.request.HoaDonSearchRequest;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.TienCocPreviewResponse;

import java.util.List;

public interface HoaDonService {
    HoaDon checkin(CheckInRequest request);

    HoaDon getHoaDonDangMoByBan(Long idBan);

    HoaDon thanhToan(Long idHoaDon);

    List<HoaDonResponse> hienThi();


    void chuyenTrangThai(Long idHoaDon);

    List<HoaDonResponse> search(HoaDonSearchRequest request);

    void huyHoaDon(Long id, HoaDonRequest request);

    HoaDonResponse hoanThanhHoaDon(Long idHoaDon);

    TienCocPreviewResponse previewHuyHoaDon(Long id);

    byte[] inHoaDon(Long id);
}
