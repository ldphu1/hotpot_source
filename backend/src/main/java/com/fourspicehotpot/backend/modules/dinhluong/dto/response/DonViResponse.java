package com.fourspicehotpot.backend.modules.dinhluong.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonViResponse {
    private Long id;
    private String maDonVi;
    private String tenDonVi;
    private Integer soLuongDinhLuong;
    private List<DinhLuongResponse> dinhLuongS;
    private List<String> danhMucs;
    private Boolean trangThaiXoa;
}
