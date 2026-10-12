package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

@Data
public class GhepBanDangDungRequest {

    private Long idHoaDonChinh;

    private Long idHoaDonGhep;

    private Long idBanChinh;

    private Long idBanGhep;
}