package com.fourspicehotpot.backend.modules.datban.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatBanSearchRequest {

    private String soDienThoai;
    private String trangThai;
    private String trangThaiCoc;
    private String tenKhuVuc;
    private LocalDate thoiGianDat;
}
