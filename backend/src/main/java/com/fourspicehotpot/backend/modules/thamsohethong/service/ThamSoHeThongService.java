package com.fourspicehotpot.backend.modules.thamsohethong.service;

import com.fourspicehotpot.backend.modules.datban.entity.DatBan;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;
import com.fourspicehotpot.backend.modules.thamsohethong.dto.request.ThamSoHeThongRequest;
import com.fourspicehotpot.backend.modules.thamsohethong.dto.response.ThamSoHeThongResponse;

import java.util.List;

public interface ThamSoHeThongService {
    List<ThamSoHeThongResponse> getAllThamSo();

    String get(String code);

    Integer getInt(String code);

    Double getDouble(String code);

    ThamSoHeThongResponse getDetail(Long id);

    ThamSoHeThongResponse create(ThamSoHeThongRequest request);

    ThamSoHeThongResponse update(Long id, ThamSoHeThongRequest request);

    void delete(Long id);

    void validateSucChuaBan(DatBan datBan, List<BanAn> danhSachBan);


}
