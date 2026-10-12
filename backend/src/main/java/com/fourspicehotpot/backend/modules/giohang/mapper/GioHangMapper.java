package com.fourspicehotpot.backend.modules.giohang.mapper;

import com.fourspicehotpot.backend.modules.giohang.dto.response.GioHangChiTietResponse;
import com.fourspicehotpot.backend.modules.giohang.dto.response.GioHangResponse;
import com.fourspicehotpot.backend.modules.giohang.entity.GioHang;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


@Component
public class GioHangMapper {


    public GioHangResponse mapToResponse(GioHang gioHang) {

        List<GioHangChiTietResponse> items = gioHang.getChiTietList()
                .stream()
                .map(ct -> {
                    BigDecimal gia = ct.getGia();
                    BigDecimal soLuong = BigDecimal.valueOf(ct.getSoLuong());

                    BigDecimal thanhTien = gia.multiply(soLuong);

                    return GioHangChiTietResponse.builder()
                            .id(ct.getId())
                            .idSanPham(ct.getSanPham().getId())
                            .tenSanPham(ct.getSanPham().getTenSanPham())
                            .gia(gia)
                            .hinhAnh(ct.getSanPham().getHinhAnh())
                            .soLuong(ct.getSoLuong())
                            .thanhTien(thanhTien)
                            .build();
                })
                .toList();

        BigDecimal tongTien = items.stream()
                .map(GioHangChiTietResponse::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return GioHangResponse.builder()
                .id(gioHang.getId())
                .idKhachHang(
                        gioHang.getKhachHang() != null
                                ? gioHang.getKhachHang().getId()
                                : null
                )
                .items(items)
                .tongTien(tongTien)
                .build();
    }
}
