package com.fourspicehotpot.backend.modules.thongke.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrangThaiResponse {

    private Integer vuaTao;

    private Integer choXacNhan;

    private Integer xacNhan;

    private Integer phucVu;

    private Integer choThanhToan;

    private Integer daThanhToan;

    private Integer hoanThanh;

    private Integer huy;
}