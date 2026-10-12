package com.fourspicehotpot.backend.modules.thucdon.service;

import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;

import java.util.List;

public interface CustomerXemThucDonService {

    List<SanPhamResponse> getAllSanPham();

    List<SanPhamResponse> getAllMonLe();

    SanPhamResponse findById(Integer id);

    List<SanPhamResponse> findSanPhamByDanhMuc(Integer id);

    List<SanPhamResponse> searchSanPham(SanPhamSearchRequest request);

    List<SanPhamResponse> getAllSetLau();

    List<SanPhamResponse> searchSetLauByNuocLau(Long idNuocLau);

    List<DanhMucResponse> findAllDanhMucHoatDong();


}
