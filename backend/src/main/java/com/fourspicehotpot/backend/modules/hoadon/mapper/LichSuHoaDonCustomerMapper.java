package com.fourspicehotpot.backend.modules.hoadon.mapper;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuDonHangResponse;
import org.springframework.stereotype.Component;

@Component
public class LichSuHoaDonCustomerMapper {

    public LichSuDonHangResponse mapToResponse(LichSuHoaDonProjection projection) {

        return LichSuDonHangResponse.builder()
                .idHoaDon(projection.getIdHoaDon())
                .maHoaDon(projection.getMaHoaDon())
                .soNguoiToiDa(projection.getSoNguoiToiDa())
                .thoiGianDat(projection.getThoiGianDat())
                .tienCoc(projection.getTienCoc())
                .tenBanAn(projection.getTenBanAn())
                .tenKhuVuc(projection.getTenKhuVuc())
                .ngayTao(projection.getNgayTao())
                .tongTien(projection.getTongTien())
                .trangThai(projection.getTrangThai())
                .build();
    }
}
