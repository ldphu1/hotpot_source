package com.fourspicehotpot.backend.modules.hoadon.mapper;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonChiTietResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerLichSuHoaDonResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import com.fourspicehotpot.backend.modules.phieugiamgia.dto.response.CustomerPhieuGiamGiaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class HoaDonDetailCustomerMapper {


    @Autowired
    private HoaDonCustomerMapper hoaDonMapper;

    public CustomerHoaDonDetailResponse mapToDetail(HoaDon hoaDon) {

        // dùng lại mapper chính
        CustomerHoaDonResponse hoaDonRes = hoaDonMapper.mapToResponse(hoaDon);

        List<CustomerHoaDonChiTietResponse> items = hoaDon.getHoaDonChiTiets()
                .stream()
                .map(hoaDonMapper::mapToChiTietResponse)
                .toList();

        List<CustomerLichSuHoaDonResponse> lichSu = hoaDon.getLichSuHoaDons()
                .stream()
                .map(ls -> CustomerLichSuHoaDonResponse.builder()
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
                .sorted(Comparator.comparing(CustomerLichSuHoaDonResponse::getThoiGian))
                .toList();
        CustomerPhieuGiamGiaResponse phieuGiamGia = null;
        if (hoaDon.getPhieuGiamGia() != null) {

            phieuGiamGia = CustomerPhieuGiamGiaResponse.builder()
                    .ma(hoaDon.getPhieuGiamGia().getMaGiamGia())
                    .ten(hoaDon.getPhieuGiamGia().getTenGiamGia())
                    .giaTri(hoaDon.getPhieuGiamGia().getGiaTriGiam())
                    .build();
        }

        return CustomerHoaDonDetailResponse.builder()
                .hoaDon(hoaDonRes)
                .items(items)
                .lichSu(lichSu)
                .phieuGiamGia(phieuGiamGia)
                .build();
    }
}

