package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class GhepBanRequest {
    private Long idHoaDon;
    private Long idBanHienTai;
    private Integer soKhachThem;
    private List<Long> listIdBanMuonGhep;
}