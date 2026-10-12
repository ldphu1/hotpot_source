package com.fourspicehotpot.backend.modules.setlau.service;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.request.SanPhamSearchRequest;
import com.fourspicehotpot.backend.modules.setlau.dto.response.SanPhamResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface SanPhamService {

    List<SanPhamResponse> getAllSanPham();

    List<SanPhamResponse> getAllMonLe();

    SanPhamResponse findById(Integer id);

    List<SanPhamResponse> findSanPhamByDanhMuc(Integer id);

    List<SanPhamResponse> getAllSetLau();

    void addSanPham(List<SanPhamRequest> request, MultipartFile files);

    void updateSanPham(Integer id, SanPhamRequest request, MultipartFile file);

    List<SanPhamResponse> searchSetLau(
            String keyword,
            Long idNuocLau,
            Boolean trangThaiXoa,
            Integer minPrice,
            Integer maxPrice
    );

    List<SanPhamResponse> searchSanPham(SanPhamSearchRequest request);

    void chuyenTrangThai(Integer id);

    List<SanPhamResponse> getSetLau();

    List<SanPhamResponse> getSanPhamDotGiamGia();

    List<SanPhamResponse> getAllSanPhamDgg();

    List<SanPhamResponse> getSanPhamDotGiamGiaByDanhmuc(Integer idDanhMuc);

    List<SanPhamResponse> getSanPhamTheoLoai(Boolean laSet);

    BigDecimal tinhGiaSauGiam(SanPham sp);
}
