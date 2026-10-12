package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class DoiBanRequest {
    private Long idHoaDon;
    private Long idBanHienTai;
    private Long idBanMoi;

    private List<Long> listIdBanMoi;

    private List<PhanBoSoKhachTheoBanRequest> phanBoSoKhachTheoBan;

    private Integer soKhachThem;
}