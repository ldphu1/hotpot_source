package com.fourspicehotpot.backend.modules.auth.service.serviceImpl;

import com.fourspicehotpot.backend.modules.auth.repository.DiaChiCustomerRepository;
import com.fourspicehotpot.backend.modules.auth.service.KhachHangCustomerService;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.CustomerKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.CustomerKhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.mapper.CustomerKhachHangMapper;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KhachHangCustomerServiceImpl implements KhachHangCustomerService {
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private CustomerKhachHangMapper khachHangMapper;
    @Autowired
    private DiaChiCustomerRepository diaChiCustomerRepository;

    @Override
    public CustomerKhachHangResponse getThongTin(String email) {
        KhachHang kh = khachHangRepository.findByEmail(email);
        return khachHangMapper.mapToResponse(kh);
    }

    @Override
    public CustomerKhachHangResponse updateThongTin(
            String email,
            CustomerKhachHangRequest request
    ) {

        KhachHang kh = khachHangRepository.findByEmail(email);

        // update thông tin khách
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setSoDienThoai(request.getSoDienThoai());

        khachHangRepository.save(kh);
        DiaChiKhachHang diaChi = diaChiCustomerRepository
                .findByKhachHangId(kh.getId())
                .stream()
                .filter(DiaChiKhachHang::getMacDinh)
                .findFirst()
                .orElse(null);
        if (diaChi == null) {
            diaChi = new DiaChiKhachHang();
            diaChi.setKhachHang(kh);
            diaChi.setMacDinh(true);
        }
        diaChi.setTinhThanh(request.getDiaChi().getTinhThanh());
        diaChi.setXaPhuong(request.getDiaChi().getXaPhuong());
        diaChi.setDiaChiChiTiet(request.getDiaChi().getDiaChiChiTiet());

        diaChiCustomerRepository.save(diaChi);
        return khachHangMapper.mapToResponse(kh);
    }
}
