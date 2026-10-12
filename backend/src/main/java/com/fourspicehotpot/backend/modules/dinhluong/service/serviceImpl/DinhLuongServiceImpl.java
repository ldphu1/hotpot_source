package com.fourspicehotpot.backend.modules.dinhluong.service.serviceImpl;

import com.fourspicehotpot.backend.modules.dinhluong.dto.response.DinhLuongResponse;
import com.fourspicehotpot.backend.modules.dinhluong.repository.DinhLuongRepository;
import com.fourspicehotpot.backend.modules.dinhluong.service.DinhLuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DinhLuongServiceImpl implements DinhLuongService {
    @Autowired
    private DinhLuongRepository dinhLuongRepository;

    @Override
    public List<DinhLuongResponse> getAllDinhLuong() {
        return dinhLuongRepository.findAll()
                .stream()
                .map(dl -> DinhLuongResponse.builder()
                        .id(dl.getId())
                        .maDinhLuong(dl.getMaDinhLuong())
                        .giaTri(dl.getGiaTri())
                        .idDonVi(dl.getDonVi().getId())
                        .tenDonVi(dl.getDonVi().getTenDonVi())
                        .trangThaiXoa(dl.getTrangThaiXoa())
                        .build()
                )
                .toList();
    }

    @Override
    public List<DinhLuongResponse> getDinhLuongByDanhMuc(Integer idDanhMuc) {
        return dinhLuongRepository.getByDanhMuc(idDanhMuc);
    }
}
