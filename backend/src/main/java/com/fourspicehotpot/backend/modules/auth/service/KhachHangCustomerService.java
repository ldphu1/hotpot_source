package com.fourspicehotpot.backend.modules.auth.service;

import com.fourspicehotpot.backend.modules.khachhang.dto.request.CustomerKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.CustomerKhachHangResponse;

public interface KhachHangCustomerService {
    CustomerKhachHangResponse getThongTin(String email);

    CustomerKhachHangResponse updateThongTin(String email, CustomerKhachHangRequest request);
}
