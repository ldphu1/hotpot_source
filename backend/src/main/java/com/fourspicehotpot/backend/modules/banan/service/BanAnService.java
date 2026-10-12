package com.fourspicehotpot.backend.modules.banan.service;

import com.fourspicehotpot.backend.modules.banan.dto.request.BanAnSerachRequest;
import com.fourspicehotpot.backend.modules.banan.dto.response.BanAnResponse;
import com.fourspicehotpot.backend.modules.banan.entity.BanAn;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BanAnService {


    List<BanAn> getDanhSachBanTheoNgay(LocalDate ngay);

    BanAn save(BanAn banAn);

    BanAn findById(Integer id);

    void deleteBanAn(Integer id);

    List<BanAn> findAll();

    List<BanAn> searchBanAn(String keyword);
    List<BanAnResponse> getDanhSachBan();

    // Không sửa
    List<BanAnResponse> getDanhSachBanChoDat(LocalDateTime thoiGianDat);

    List<BanAnResponse> filterBanAn(BanAnSerachRequest request, LocalDateTime thoiGianDat);

}
