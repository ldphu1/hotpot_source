package com.fourspicehotpot.backend.modules.thamsohethong.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThamSoHeThongResponse {

    private Long id;
    private String maThamSo;
    private String tenThamSo;
    private String giaTri;
    private String kieuDuLieu;
    private String nhom;
    private String moTa;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}