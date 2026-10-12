package com.fourspicehotpot.backend.modules.hoadon.dto.request;

import lombok.Data;

@Data
public class LichSuHoaDonRequest {

    private Long idHoaDon;

    private String hanhDong;

    private Integer trangThai;

    private String ghiChu;
}