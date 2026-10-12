package com.fourspicehotpot.backend.modules.setlau.mapper;

import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SanPhamMapper {

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

    public SanPhamResponse mapFromObjectArray(Object[] o) {
        SanPhamResponse res = new SanPhamResponse();

        res.setId(o[0] != null ? ((Number) o[0]).longValue() : null);
        res.setTenSanPham((String) o[1]);

        BigDecimal gia = o[2] != null
                ? new BigDecimal(((Number) o[2]).toString())
                : BigDecimal.ZERO;

        Boolean laSet = o[3] != null && Boolean.parseBoolean(o[3].toString());

        BigDecimal giaTriGiam = o[4] != null
                ? new BigDecimal(o[4].toString())
                : null;

        BigDecimal soTienGiam = o[5] != null
                ? new BigDecimal(o[5].toString())
                : null;

        res.setGia(gia);
        res.setLaSet(laSet);
        res.setGiaSauGiam(soTienGiam != null ? gia.subtract(soTienGiam) : gia);
        res.setHinhAnh((String) o[6]);
        res.setTenDanhMuc(o[7] != null ? (String) o[7] : null);


        return res;
    }

}
