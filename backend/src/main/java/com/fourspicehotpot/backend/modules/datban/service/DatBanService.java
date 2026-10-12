package com.fourspicehotpot.backend.modules.datban.service;

import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.DatBanSearchRequest;
import com.fourspicehotpot.backend.modules.checkin.dto.request.SearchKhachChoCheckInRequest;
import com.fourspicehotpot.backend.modules.datban.dto.request.XepBanRequest;
import com.fourspicehotpot.backend.modules.datban.dto.response.DatBanResponse;

import java.util.List;

public interface DatBanService {

    List<DatBanResponse> findAll();

    DatBanResponse findById(Long id);

    DatBanResponse xacNhanDatBan(Long id);

    DatBanResponse xacNhanCocVaXacNhan(DatBanRequest request);

    DatBanResponse datBanTaiQuay(DatBanRequest request);

    List<DatBanResponse> findForCheckIn();

    DatBanResponse xepBanTaiQuay(XepBanRequest request);

    DatBanResponse xepLaiBan(XepBanRequest request);

    boolean dangDuocSuDung(Integer idBan);

    List<DatBanResponse> searchDatBan(DatBanSearchRequest request);

    void autoHuyDatBan();

}
