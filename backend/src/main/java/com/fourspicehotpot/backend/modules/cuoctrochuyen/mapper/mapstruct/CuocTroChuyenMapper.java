package com.fourspicehotpot.backend.modules.cuoctrochuyen.mapper.mapstruct;

import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.CuocTroChuyenResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.LoaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.TrangThaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CuocTroChuyenMapper {

    CuocTroChuyenResponse mapToResponse(CuocTroChuyen cuocTroChuyen);

    List<CuocTroChuyenResponse> mapToResponse(List<CuocTroChuyen> cuocTroChuyen);

    default CuocTroChuyen toEntity(Long idKhachHang, String tenKhach) {
        return CuocTroChuyen.builder()
                .khachHang(idKhachHang != null ? KhachHang.builder().id(idKhachHang).build() : null)
                .tenKhach(tenKhach)
                .loai(String.valueOf(LoaiCuocTroChuyen.AI))
                .trangThai(TrangThaiCuocTroChuyen.DANG_MO)
                .build();
    }
}
