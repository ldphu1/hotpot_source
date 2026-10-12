package com.fourspicehotpot.backend.modules.dinhluong.service;

import com.fourspicehotpot.backend.modules.dinhluong.dto.request.DonViRequest;
import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DonViResponse;

import java.util.List;

public interface DonViService {

    List<DonViResponse> getAllDonVi();

    List<DonViResponse> searchDonVi(String keyword, Boolean trangThaiXoa);

    void addDonVi(DonViRequest request);

    void chuyenTrangThai(Long id);

    DonViResponse findById(Long id);

    void updateDonViTongHop(Long id, DonViRequest request);
}
