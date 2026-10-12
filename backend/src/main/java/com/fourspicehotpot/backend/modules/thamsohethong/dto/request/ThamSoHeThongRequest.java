package com.fourspicehotpot.backend.modules.thamsohethong.dto.request;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThamSoHeThongRequest {

    private String maThamSo;
    private String tenThamSo;
    private String giaTri;
    private String kieuDuLieu;
    private String nhom;
    private String moTa;
    private Boolean trangThai;
}
