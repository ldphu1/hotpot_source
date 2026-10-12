package com.fourspicehotpot.backend.modules.khachhang.mapper;

import com.fourspicehotpot.backend.modules.auth.repository.DiaChiCustomerRepository;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.CustomerDiaChiKhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.CustomerKhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomerKhachHangMapper {

    @Autowired
    private DiaChiCustomerRepository diaChiCustomerRepository;

    public CustomerKhachHangResponse mapToResponse(KhachHang khachHang) {

        List<DiaChiKhachHang> list =
                diaChiCustomerRepository.findByKhachHangId(khachHang.getId());

        List<CustomerDiaChiKhachHangResponse> addressResponses = list.stream()
                .map(dc -> CustomerDiaChiKhachHangResponse.builder()
                        .id(dc.getId())
                        .diaChiChiTiet(dc.getDiaChiChiTiet())
                        .xaPhuong(dc.getXaPhuong())
                        .tinhThanh(dc.getTinhThanh())
                        .macDinh(dc.getMacDinh())
                        .build())
                .toList();

        CustomerDiaChiKhachHangResponse defaultAddress = addressResponses.stream()
                .filter(CustomerDiaChiKhachHangResponse::getMacDinh)
                .findFirst()
                .orElse(null);

        return CustomerKhachHangResponse.builder()
                .id(khachHang.getId())
                .tenKhachHang(khachHang.getTenKhachHang())
                .soDienThoai(khachHang.getSoDienThoai())
                .ngaySinh(khachHang.getNgaySinh())
                .email(khachHang.getEmail())
                .gioiTinh(khachHang.getGioiTinh())
                .defaultAddress(defaultAddress)
                .addresses(addressResponses)
                .build();
    }

}
