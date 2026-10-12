package com.fourspicehotpot.backend.modules.thucdon.service;

import com.fourspicehotpot.backend.modules.thucdon.dto.request.DanhMucRequest;
import com.fourspicehotpot.backend.modules.thucdon.dto.response.DanhMucResponse;
import com.fourspicehotpot.backend.modules.thucdon.entity.DanhMuc;

import java.util.List;

public interface DanhMucService {


    List<DanhMucResponse> findAllDanhMucHoatDong();

    List<DanhMucResponse> findAllDanhMucMonLe();

    void addDanhMuc(DanhMucRequest request);

    DanhMuc findDanhMucById(Integer id);

    DanhMucResponse getDanhMucById(Integer id);

    void updateDanhMuc(DanhMucRequest request);

    void deleteDanhMuc(Integer id);

    void restoreDanhMuc(Integer id);

    List<DanhMucResponse> filterDanhMuc(String keyword, Integer idDanhMuc, Boolean trangThaiXoa);
}
