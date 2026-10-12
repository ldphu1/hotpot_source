package com.fourspicehotpot.backend.modules.khachhang.service.serviceImpl;


import com.fourspicehotpot.backend.modules.khachhang.service.DiaChiKhachHangService;
import com.fourspicehotpot.backend.modules.khachhang.dto.request.DiaChiKhachHangRequest;
import com.fourspicehotpot.backend.modules.khachhang.dto.response.DiaChiKhachHangResponse;
import com.fourspicehotpot.backend.modules.khachhang.entity.DiaChiKhachHang;
import com.fourspicehotpot.backend.modules.khachhang.entity.KhachHang;
import com.fourspicehotpot.backend.modules.khachhang.repository.DiaChiRepository;
import com.fourspicehotpot.backend.modules.khachhang.repository.KhachHangRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaChiKhachHangServiceImpl implements DiaChiKhachHangService {
    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<DiaChiKhachHangResponse> getByKhachHangId(Long khId) {

        return diaChiRepository.findByKhachHangId(khId)
                .stream()
                .map(dc -> {
                    DiaChiKhachHangResponse r = new DiaChiKhachHangResponse();
                    r.setId(dc.getId());
                    r.setHoTenNguoiNhan(dc.getHoTenNguoiNhan());
                    r.setSdtNguoiNhan(dc.getSdtNguoiNhan());
                    r.setTinhThanh(dc.getTinhThanh());
                    r.setXaPhuong(dc.getXaPhuong());
                    r.setDiaChiChiTiet(dc.getDiaChiChiTiet());
                    r.setMacDinh(dc.getMacDinh());
                    return r;
                }).toList();
    }

    @Override
    public DiaChiKhachHangResponse add(Long khId, DiaChiKhachHangRequest req) {

        KhachHang kh = khachHangRepository.findById(khId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (Boolean.TRUE.equals(req.getMacDinh())) {
            diaChiRepository.clearMacDinhByKhachHang(khId);
        }

        DiaChiKhachHang dc = new DiaChiKhachHang();
        dc.setKhachHang(kh);

        dc.setHoTenNguoiNhan(req.getHoTenNguoiNhan());
        dc.setSdtNguoiNhan(req.getSdtNguoiNhan());

        dc.setTinhThanh(req.getTinhThanh());
        dc.setXaPhuong(req.getXaPhuong());
        dc.setDiaChiChiTiet(req.getDiaChiChiTiet());
        dc.setMacDinh(req.getMacDinh());

        diaChiRepository.save(dc);

        DiaChiKhachHangResponse res = new DiaChiKhachHangResponse();
        res.setId(dc.getId());
        res.setHoTenNguoiNhan(dc.getHoTenNguoiNhan());
        res.setSdtNguoiNhan(dc.getSdtNguoiNhan());
        res.setTinhThanh(dc.getTinhThanh());
        res.setXaPhuong(dc.getXaPhuong());
        res.setDiaChiChiTiet(dc.getDiaChiChiTiet());
        res.setMacDinh(dc.getMacDinh());

        return res;
    }

    @Override
    @Transactional
    public void setMacDinh(Long diaChiId) {

        DiaChiKhachHang dc = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        Long khId = dc.getKhachHang().getId();

        diaChiRepository.clearMacDinhByKhachHang(khId);

        dc.setMacDinh(true);

        diaChiRepository.save(dc);
    }

    @Override
    @Transactional
    public DiaChiKhachHangResponse update(Long diaChiId, DiaChiKhachHangRequest req) {

        DiaChiKhachHang dc = diaChiRepository.findById(diaChiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        if (Boolean.TRUE.equals(req.getMacDinh())) {
            diaChiRepository.clearMacDinhByKhachHang(dc.getKhachHang().getId());
        }

        dc.setHoTenNguoiNhan(req.getHoTenNguoiNhan());
        dc.setSdtNguoiNhan(req.getSdtNguoiNhan());
        dc.setTinhThanh(req.getTinhThanh());
        dc.setXaPhuong(req.getXaPhuong());
        dc.setDiaChiChiTiet(req.getDiaChiChiTiet());
        dc.setMacDinh(req.getMacDinh());

        diaChiRepository.save(dc);

        DiaChiKhachHangResponse res = new DiaChiKhachHangResponse();
        res.setId(dc.getId());
        res.setHoTenNguoiNhan(dc.getHoTenNguoiNhan());
        res.setSdtNguoiNhan(dc.getSdtNguoiNhan());
        res.setTinhThanh(dc.getTinhThanh());
        res.setXaPhuong(dc.getXaPhuong());
        res.setDiaChiChiTiet(dc.getDiaChiChiTiet());
        res.setMacDinh(dc.getMacDinh());

        return res;
    }
}
