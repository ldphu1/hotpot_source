package com.fourspicehotpot.backend.modules.checkin.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BanAnTrongResponse {
    private Integer idBanAn;
    private String maBanAn;
    private String tenBanAn;
    private Integer sucChuaToiDa;
    private Integer trangThai;
    private Integer khuVucId;
    private String tenKhuVuc;
}
