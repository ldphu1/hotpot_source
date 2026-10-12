package com.fourspicehotpot.backend.modules.khachhang.service;


import com.fourspicehotpot.backend.modules.khachhang.dto.request.DiaChiKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.DiaChiKhachHangResponse;

import java.util.List;

public interface DiaChiKhachHangService {
    List<DiaChiKhachHangResponse> getByKhachHangId(Long khId);

    DiaChiKhachHangResponse add(Long khId, DiaChiKhachHangRequest req);

    DiaChiKhachHangResponse update(Long diaChiId, DiaChiKhachHangRequest req);

    void setMacDinh(Long diaChiId);
}
