package com.fourspicehotpot.backend.modules.hoadon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LichSuDonHangResponse {

    private Long idHoaDon;
    private String maHoaDon;
    private Integer trangThai;
    private String tenTrangThai;
    private BigDecimal tongTien;
    private Integer soNguoiToiDa;
    private LocalDateTime ngayTao;
    private BigDecimal tienCoc;
    private LocalDateTime thoiGianDat;
    private String tenBanAn;
    private String tenKhuVuc;
}
