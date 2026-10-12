package com.fourspicehotpot.backend.modules.cuoctrochuyen.service;

import com.fourspicehotpot.backend.common.dto.ApiResponseCommon;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.ReplyTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.SearchCuocTroChuyenRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.CuocTroChuyenProjection;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.page.CursorPageResponse;

public interface CuocTroChuyenService {

    ApiResponseCommon<CursorPageResponse<CuocTroChuyenProjection>> getAllCuocTroChuyen(SearchCuocTroChuyenRequest request);

    ApiResponseCommon<ChatRealtimeResponse> replyTinNhan(ReplyTinNhanRequest request, Long idNhanVien);

    ApiResponseCommon<Void> nhanXuLy(Long idCuocTroChuyen, Long idNhanVien);

    ApiResponseCommon<Void> ketThucXuLy(Long idCuocTroChuyen);
}
