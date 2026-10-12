package com.fourspicehotpot.backend.modules.thongke.service;

import com.fourspicehotpot.backend.modules.thongke.dto.response.*;

import java.time.LocalDateTime;
import java.util.List;

public interface ThongKeService {

    ThongKeDoanhThuResponse layThongKeDoanhThu();

    ThongKeTongQuanResponse layThongKeTongQuan(LocalDateTime from, LocalDateTime to);

    List<DoanhThuTheoThangResponse> thongKeDoanhThuTheoThang();

    List<TopSetResponse> getTopSet();

    BookingChannelResponse getBookingChannel();
    TrangThaiResponse getTrangThai();
}
