package com.fourspicehotpot.backend.modules.app.service.impl;

import com.fourspicehotpot.backend.modules.app.dto.response.DotGiamGiaAppResponse;
import com.fourspicehotpot.backend.modules.app.dto.response.PhieuGiamGiaAppResponse;
import com.fourspicehotpot.backend.modules.app.dto.response.SanPhamGiamGiaAppResponse;
import com.fourspicehotpot.backend.modules.app.service.AppDiscountService;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGia;
import com.fourspicehotpot.backend.modules.dotgiamgia.entity.DotGiamGiaSanPham;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaRepository;
import com.fourspicehotpot.backend.modules.dotgiamgia.repository.DotGiamGiaSanPhamRepository;
import com.fourspicehotpot.backend.modules.phieugiamgia.entity.PhieuGiamGia;
import com.fourspicehotpot.backend.modules.phieugiamgia.repository.PhieuGiamGiaRepository;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppDiscountServiceImpl implements AppDiscountService {

    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    @Override
    public List<PhieuGiamGiaAppResponse> getActiveCoupons() {
        List<PhieuGiamGia> coupons = phieuGiamGiaRepository.findActiveCoupons(LocalDate.now());
        return coupons.stream().map(c -> PhieuGiamGiaAppResponse.builder()
                .id(c.getId())
                .maGiamGia(c.getMaGiamGia())
                .tenGiamGia(c.getTenGiamGia())
                .kieuGiamGia(c.getKieuGiamGia())
                .phuongThucGiamGia(c.getPhuongThucGiamGia())
                .giaTriGiam(c.getGiaTriGiam())
                .giamToiDa(c.getGiamToiDa())
                .dieuKienSuDung(c.getDieuKienSuDung())
                .ngayBatDau(c.getNgayBatDau())
                .ngayKetThuc(c.getNgayKetThuc())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<DotGiamGiaAppResponse> getActiveCampaigns() {
        List<DotGiamGia> campaigns = dotGiamGiaRepository.findActiveCampaigns(LocalDateTime.now());
        return campaigns.stream().map(c -> DotGiamGiaAppResponse.builder()
                .id(c.getId())
                .maDot(c.getMaDot())
                .tenDot(c.getTenDot())
                .giaTriGiam(c.getGiaTriGiam())
                .ngayBatDau(c.getNgayBatDau())
                .ngayKetThuc(c.getNgayKetThuc())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<SanPhamGiamGiaAppResponse> getDiscountedProducts() {
        List<DotGiamGiaSanPham> mappingList = dotGiamGiaSanPhamRepository.findAllActiveDiscountedProducts(LocalDateTime.now());
        return mappingList.stream().map(m -> {
            DotGiamGia campaign = m.getDotGiamGia();
            SanPham product = m.getSanPham();
            
            BigDecimal giaGoc = product.getGia();
            BigDecimal giaTriGiam = campaign.getGiaTriGiam() != null ? campaign.getGiaTriGiam() : BigDecimal.ZERO;
            
            // Tính giá sau giảm. Giả định giaTriGiam là % (nhỏ hơn hoặc bằng 100).
            BigDecimal giaSauGiam;
            if (giaTriGiam.compareTo(new BigDecimal("100")) <= 0) {
                // Giảm theo phần trăm
                BigDecimal percent = new BigDecimal("100").subtract(giaTriGiam).divide(new BigDecimal("100"));
                giaSauGiam = giaGoc.multiply(percent);
            } else {
                // Giảm theo số tiền
                giaSauGiam = giaGoc.subtract(giaTriGiam);
                if (giaSauGiam.compareTo(BigDecimal.ZERO) < 0) {
                    giaSauGiam = BigDecimal.ZERO;
                }
            }

            return SanPhamGiamGiaAppResponse.builder()
                    .id(product.getId())
                    .maSanPham(product.getMaSanPham())
                    .tenSanPham(product.getTenSanPham())
                    .hinhAnh(product.getHinhAnh())
                    .giaGoc(giaGoc)
                    .giaSauGiam(giaSauGiam)
                    .laSet(product.getLaSet())
                    .tenDotGiamGia(campaign.getTenDot())
                    .giaTriGiamCuaDot(giaTriGiam)
                    .build();
        }).collect(Collectors.toList());
    }
}
