package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CheckInRequest {
    private Integer idDatBan;
    private Integer idBanAn;
    private Long idNhanVien;
    private Integer soKhach;
    private String ghiChu;

    private List<Integer> listIdBanAn;
    private Boolean isQuickCheckIn; // Fix: Báo cho backend biết đây là check-in nhanh để tạo hóa đơn mới
}