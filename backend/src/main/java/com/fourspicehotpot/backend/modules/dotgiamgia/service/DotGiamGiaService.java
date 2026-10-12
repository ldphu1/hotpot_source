package com.fourspicehotpot.backend.modules.dotgiamgia.service;

import com.fourspicehotpot.backend.modules.dotgiamgia.dto.request.DotGiamGiaRequest;
import com.fourspicehotpot.backend.modules.dotgiamgia.dto.response.DotGiamGiaResponse;

import java.util.List;

public interface DotGiamGiaService {
    List<DotGiamGiaResponse> getAll();

    DotGiamGiaResponse getById(Long id);

    void delete(Long id); // soft delete

    DotGiamGiaResponse getActiveBySanPham(Long sanPhamId);

    DotGiamGiaResponse create(DotGiamGiaRequest request);

    DotGiamGiaResponse update(Long id, DotGiamGiaRequest request);

    List<DotGiamGiaResponse> filter(String keyword, Integer trangThai);

    public void doiTrangThai(Long id);

}
