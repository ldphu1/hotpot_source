package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonDetailResponse {
    private HoaDonResponse hoaDon;
    private List<HoaDonChiTietResponse> items;
    private List<LichSuHoaDonResponse> lichSu;
}
