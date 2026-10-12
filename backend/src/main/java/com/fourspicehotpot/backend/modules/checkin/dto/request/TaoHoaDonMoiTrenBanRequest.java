package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

@Data
public class TaoHoaDonMoiTrenBanRequest {
    private Integer idBanAn;
    private Integer soKhach;
    private Long idNhanVien;
    private String ghiChu;
}