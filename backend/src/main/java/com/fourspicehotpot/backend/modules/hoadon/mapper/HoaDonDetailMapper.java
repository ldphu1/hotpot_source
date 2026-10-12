package com.fourspicehotpot.backend.modules.hoadon.mapper;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class HoaDonDetailMapper {


    @Autowired
    private HoaDonMapper hoaDonMapper;

    public HoaDonDetailResponse mapToDetail(HoaDon hoaDon) {

        // dùng lại mapper chính
        HoaDonResponse hoaDonRes = hoaDonMapper.mapToResponse(hoaDon);

        List<HoaDonChiTietResponse> items = hoaDon.getHoaDonChiTiets()
                .stream()
                .map(hoaDonMapper::mapToChiTietResponse)
                .toList();

        List<LichSuHoaDonResponse> lichSu = hoaDon.getLichSuHoaDons()
                .stream()
                .map(ls -> LichSuHoaDonResponse.builder()
                        .id(ls.getId())
                        .hanhDong(ls.getHanhDong())
                        .trangThai(ls.getTrangThai())
                        .ghiChu(ls.getGhiChu())
                        .tenNhanVien(
                                Optional.ofNullable(ls.getNhanVien())
                                        .map(NhanVien::getTenNhanVien)
                                        .orElse("Hệ thống")
                        )
                        .thoiGian(ls.getThoiGian())
                        .build()
                )
                .sorted(Comparator.comparing(LichSuHoaDonResponse::getThoiGian))
                .toList();

        return HoaDonDetailResponse.builder()
                .hoaDon(hoaDonRes)
                .items(items)
                .lichSu(lichSu)
                .build();
    }
}

