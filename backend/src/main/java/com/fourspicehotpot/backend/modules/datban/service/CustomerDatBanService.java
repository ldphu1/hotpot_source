package com.fourspicehotpot.backend.modules.datban.service;

import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.datban.dto.request.CustomerDatBanRequest;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;

public interface CustomerDatBanService {


    public String datBanOnline(CustomerDatBanRequest request, HttpServletRequest httpRequest);

    BigDecimal tinhGiaSauGiam(SanPham sp);
}
