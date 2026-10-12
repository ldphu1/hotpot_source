package com.fourspicehotpot.backend.modules.thucdon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DanhMucResponse {
    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private Boolean trangThaiXoa;
    private List<Integer> idDonVi;
    private Long soLuongMon;

    public DanhMucResponse(Integer id, String maDanhMuc, String tenDanhMuc, Boolean trangThaiXoa, Long soLuongMon) {
        this.id = id;
        this.maDanhMuc = maDanhMuc;
        this.tenDanhMuc = tenDanhMuc;
        this.trangThaiXoa = trangThaiXoa;
        this.soLuongMon = soLuongMon;
    }
}
