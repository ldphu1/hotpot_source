package com.fourspicehotpot.backend.modules.cuoctrochuyen.service;

import com.fourspicehotpot.backend.common.dto.ApiResponseCommon;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.GuiTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.TinNhanResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.page.CursorPageResponse;

import java.time.LocalDateTime;

public interface TinNhanService {
    ApiResponseCommon<CursorPageResponse<TinNhanResponse>> getTinNhan(Long idCuocTroChuyen, LocalDateTime cursor);

    CuocTroChuyen taoCuocTroChuyenVangLai(String sessionId);

    ApiResponseCommon<ChatRealtimeResponse> guiTinNhan(GuiTinNhanRequest request, Long idKhachHang);

    CuocTroChuyen timHoacTaoCuocTroChuyen(GuiTinNhanRequest request, Long idKhachHang);


}
