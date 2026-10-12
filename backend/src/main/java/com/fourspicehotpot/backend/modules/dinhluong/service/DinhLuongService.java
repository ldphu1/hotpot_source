package com.fourspicehotpot.backend.modules.dinhluong.service;

import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse;

import java.util.List;

public interface DinhLuongService {

    List<DinhLuongResponse> getAllDinhLuong();

    public List<DinhLuongResponse> getDinhLuongByDanhMuc(Integer idDanhMuc);
}
