package com.fourspicehotpot.backend.modules.phieugiamgia.service;

import com.fourspicehotpot.backend.modules.phieugiamgia.dto.request.ThemPhieuGiamGiaRequest;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PhieuGiamGiaService {
    List<PhieuGiamGia> findAllPhieuGiamGia();

    void addPhieuGiamGia(ThemPhieuGiamGiaRequest req);

    Map<String, Object> findPhieuGiamGiaById(Integer id);

    void updatePhieuGiamGia(Integer id, ThemPhieuGiamGiaRequest req);

    void deletePhieuGiamGia(Integer id);

    void restorePhieuGiamGia(Integer id);

    Page<PhieuGiamGia> filterPhieuGiamGia(
            String keyword,
            Integer phuongThucGiamGia,
            Integer kieuGiamGia,
            Integer trangThai,
            Pageable pageable
    );
}
