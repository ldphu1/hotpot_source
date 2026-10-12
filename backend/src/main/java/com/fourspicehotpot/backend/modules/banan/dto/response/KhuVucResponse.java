package com.fourspicehotpot.backend.modules.banan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KhuVucResponse {
    private Integer id;
    private String maKhuVuc;
    private String tenKhuVuc;
    private String viTri;
    private String moTa;
    private Integer trangThai;
}
