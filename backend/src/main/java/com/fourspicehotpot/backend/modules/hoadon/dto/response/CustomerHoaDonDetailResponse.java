package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import com.fourspicehotpot.backend.modules.phieugiamgia.dto.response.CustomerPhieuGiamGiaResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerHoaDonDetailResponse {
    private CustomerHoaDonResponse hoaDon;
    private List<CustomerHoaDonChiTietResponse> items;
    private List<CustomerLichSuHoaDonResponse> lichSu;
    private CustomerPhieuGiamGiaResponse phieuGiamGia;
}
