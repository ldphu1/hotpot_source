package com.fourspicehotpot.backend.modules.hoadon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.hoadon.service.LichSuThanhToanService;
import com.fourspicehotpot.backend.common.enums.LoaiThanhToan;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.LichSuThanhToan;
import com.fourspicehotpot.backend.modules.hoadon.repository.LichSuThanhToanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class LichSuThanhToanServiceImpl implements LichSuThanhToanService {


    @Autowired
    private LichSuThanhToanRepository repository;

    @Override
    public void save(HoaDon hoaDon,
                     String phuongThuc,
                     BigDecimal soTien,
                     String ghiChu,
                     LoaiThanhToan loaiThanhToan) {

        if (hoaDon == null) {
            throw new RuntimeException("Hóa đơn không tồn tại");
        }

        LichSuThanhToan ls = LichSuThanhToan.builder()
                .hoaDon(hoaDon)
                .soTien(soTien)
                .phuongThuc(phuongThuc)
                .trangThai(1)
                .thoiGian(LocalDateTime.now())
                .ghiChu(ghiChu)
                .loaiThanhToan(loaiThanhToan)
                .build();

        repository.save(ls);
    }
}
