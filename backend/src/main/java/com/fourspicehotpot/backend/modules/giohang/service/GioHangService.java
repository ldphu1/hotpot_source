package com.fourspicehotpot.backend.modules.giohang.service;

import com.fourspicehotpot.backend.modules.giohang.dto.request.GioHangRequest;
import com.fourspicehotpot.backend.modules.giohang.dto.response.GioHangResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface GioHangService {
    GioHangResponse themVaoGioHang(GioHangRequest request, HttpServletRequest httpRequest);
}
