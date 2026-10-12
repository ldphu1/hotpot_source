package com.fourspicehotpot.backend.modules.hoadon.service.serviceImpl;

import com.fourspicehotpot.backend.modules.hoadon.dto.response.CustomerHoaDonDetailResponse;
import com.fourspicehotpot.backend.modules.hoadon.dto.response.LichSuDonHangResponse;
import com.fourspicehotpot.backend.modules.hoadon.entity.HoaDon;
import com.fourspicehotpot.backend.modules.hoadon.mapper.HoaDonDetailCustomerMapper;
import com.fourspicehotpot.backend.modules.hoadon.mapper.LichSuHoaDonCustomerMapper;
import com.fourspicehotpot.backend.modules.hoadon.mapper.LichSuHoaDonProjection;
import com.fourspicehotpot.backend.modules.hoadon.repository.HoaDonRepository;
import com.fourspicehotpot.backend.modules.hoadon.service.CustomerXemLichSuHoaDonService;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerXemLichSuHoaDonServiceImpl implements CustomerXemLichSuHoaDonService {
    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private HoaDonDetailCustomerMapper hoaDonDetailCustomerMapper;
    @Autowired
    private LichSuHoaDonCustomerMapper lichSuHoaDonCustomerMapper;

    @Override
    public List<LichSuDonHangResponse> getLichSu(String email) {

        KhachHang kh = khachHangRepository.findByEmail(email);

        Long idKhachHang = kh.getId();

        return hoaDonRepository.getLichSuDonHang(idKhachHang)
                .stream()
                .map(p -> LichSuDonHangResponse.builder()
                        .idHoaDon(p.getIdHoaDon())
                        .maHoaDon(p.getMaHoaDon())
                        .trangThai(p.getTrangThai())
                        .tongTien(p.getTongTien())
                        .soNguoiToiDa(p.getSoNguoiToiDa())
                        .ngayTao(p.getNgayTao())
                        .tienCoc(p.getTienCoc())
                        .thoiGianDat(p.getThoiGianDat())
                        .tenBanAn(p.getTenBanAn())
                        .tenKhuVuc(p.getTenKhuVuc())
                        .build())
                .toList();
    }

    @Override
    public CustomerHoaDonDetailResponse getChiTiet(Long idHoaDon, String email) {
        HoaDon hoaDon = hoaDonRepository.findDetailById(idHoaDon).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        return hoaDonDetailCustomerMapper.mapToDetail(hoaDon);
    }

    @Override
    public LichSuDonHangResponse getLichSuByEmailAndMaHoaDon(String email, String maHoaDon) {
        KhachHang kh = khachHangRepository.findByEmail(email);
        if (kh == null) {
            throw new RuntimeException("Không tìm thấy khách hàng");
        }
        Long idKhachHang = kh.getId();
        LichSuHoaDonProjection projection = hoaDonRepository
                .getLichSuDonHangByMaHoaDon(idKhachHang, maHoaDon).orElseThrow(() -> new RuntimeException("KHông tìm thấy"));

        return lichSuHoaDonCustomerMapper.mapToResponse(projection);
    }


}
