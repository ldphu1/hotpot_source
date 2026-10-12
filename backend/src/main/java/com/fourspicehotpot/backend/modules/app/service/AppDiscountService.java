package com.fourspicehotpot.backend.modules.app.service;

import com.fourspicehotpot.backend.modules.app.dto.response.DotGiamGiaAppResponse;
import com.fourspicehotpot.backend.modules.app.dto.response.PhieuGiamGiaAppResponse;
import com.fourspicehotpot.backend.modules.app.dto.response.SanPhamGiamGiaAppResponse;

import java.util.List;

public interface AppDiscountService {

    List<PhieuGiamGiaAppResponse> getActiveCoupons();

    List<DotGiamGiaAppResponse> getActiveCampaigns();

    List<SanPhamGiamGiaAppResponse> getDiscountedProducts();
}
