package com.fourspicehotpot.backend.modules.hoadon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.hoadon.service.HoaDonChiTietService;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.HoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDonChiTiet;
import com.fourspicehotpot.backend.modules.hoadon.mapper.HoaDonDetailMapper;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonChiTietRepository;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.setlau.entity.SanPham;
import com.fourspicehotpot.backend.modules.setlau.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {
    @Autowired
    HoaDonChiTietRepository hoaDonChiTietRepository;

    @Autowired
    HoaDonRepository hoaDonRepository;

    @Autowired
    SanPhamRepository sanPhamRepository;

    @Autowired
    private HoaDonDetailMapper hoaDonDetailMapper;


    @Override
    public HoaDonChiTiet themMon(Long idHoaDon, Long idSanPham, Integer soLuong) {

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        SanPham sanPham = sanPhamRepository.findById(idSanPham)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        BigDecimal donGia = sanPham.getGia();
        BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuong));

        HoaDonChiTiet hdct = HoaDonChiTiet.builder()
                .hoaDon(hoaDon)
                .sanPham(sanPham)
                .soLuong(soLuong)
                .donGia(donGia)
                .thanhTien(thanhTien)
                .ngayTao(LocalDateTime.now())
                .trangThai(1)
                .build();

        hoaDonChiTietRepository.save(hdct);

        // update tổng tiền
        hoaDon.setTongTien(
                hoaDon.getTongTien().add(thanhTien)
        );

        hoaDonRepository.save(hoaDon);

        return hdct;
    }

    @Override
    public HoaDonChiTiet tangSoLuong(Long idHoaDonChiTiet) {

        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món"));

        hdct.setSoLuong(hdct.getSoLuong() + 1);

        BigDecimal thanhTien = hdct.getDonGia()
                .multiply(BigDecimal.valueOf(hdct.getSoLuong()));

        hdct.setThanhTien(thanhTien);

        return hoaDonChiTietRepository.save(hdct);
    }

    @Override
    public HoaDonChiTiet giamSoLuong(Long idHoaDonChiTiet) {

        HoaDonChiTiet hdct = hoaDonChiTietRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món"));

        if (hdct.getSoLuong() > 1) {
            hdct.setSoLuong(hdct.getSoLuong() - 1);
        }

        BigDecimal thanhTien = hdct.getDonGia()
                .multiply(BigDecimal.valueOf(hdct.getSoLuong()));

        hdct.setThanhTien(thanhTien);

        return hoaDonChiTietRepository.save(hdct);
    }

    @Override
    public void xoaMon(Long idHoaDonChiTiet) {

        hoaDonChiTietRepository.deleteById(idHoaDonChiTiet);

    }

    // Không được sửa - Lãnh Minh Hiếu
    @Override
    public HoaDonDetailResponse getDetail(Long id) {
        HoaDon hoaDon = hoaDonRepository.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
        return hoaDonDetailMapper.mapToDetail(hoaDon);
    }
}
