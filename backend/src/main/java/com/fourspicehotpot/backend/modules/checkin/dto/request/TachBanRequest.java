package com.fourspicehotpot.backend.modules.checkin.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class TachBanRequest {
    private Long idHoaDon;
    private Long idBanGoc;

    // Danh sách các bàn sẽ chuyển khách tới kèm số lượng khách
    private List<ChiTietTachBan> danhSachBanMoi;

    @Data
    public static class ChiTietTachBan {
        private Long idBan;
        private Integer soKhach;
    }
}