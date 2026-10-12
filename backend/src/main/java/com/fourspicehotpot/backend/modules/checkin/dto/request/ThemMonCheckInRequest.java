package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

@Data
public class ThemMonCheckInRequest {
    private Long idHoaDon;
    private Long idSanPham;
    private Integer soLuong;
    private String tenBan;
    private Long idBanAn;

}