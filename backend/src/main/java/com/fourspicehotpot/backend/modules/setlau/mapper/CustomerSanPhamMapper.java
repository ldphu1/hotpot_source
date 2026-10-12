package com.fourspicehotpot.backend.modules.setlau.mapper;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerSanPhamMapper {
    public SanPhamResponse mapToResponse(SanPham sp) {

        return SanPhamResponse.builder()
                .id(sp.getId())
                .maSanPham(sp.getMaSanPham())
                .tenSanPham(sp.getTenSanPham())
                .gia(sp.getGia())
                .moTa(sp.getMoTa())
                .hinhAnh(sp.getHinhAnh())
                .laSet(sp.getLaSet())
                .trangThaiXoa(sp.getTrangThaiXoa())
                .idDanhMuc(
                        sp.getDanhMuc() != null ? sp.getDanhMuc().getId() : null
                )
                .tenDanhMuc(
                        sp.getDanhMuc() != null ? sp.getDanhMuc().getTenDanhMuc() : null
                )

                .idDinhLuong(
                        sp.getDinhLuong() != null ? sp.getDinhLuong().getId() : null
                )
                .giaTri(
                        sp.getDinhLuong() != null ? sp.getDinhLuong().getGiaTri() : null
                )
                .idDonVi(
                        sp.getDinhLuong() != null &&
                                sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getId()
                                : null
                )
                .tenDonVi(
                        sp.getDinhLuong() != null &&
                                sp.getDinhLuong().getDonVi() != null
                                ? sp.getDinhLuong().getDonVi().getTenDonVi()
                                : null
                )
                .build();
    }
}
