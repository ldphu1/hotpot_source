package com.fourspicehotpot.backend.modules.cuoctrochuyen.service.serviceImpl;

import com.fourspicehotpot.backend.common.dto.ApiResponseCommon;
import com.fourspicehotpot.backend.infrastructure.constant.CursorPageConstants;
import com.fourspicehotpot.backend.integration.websocket.service.WebSocketService;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.ReplyTinNhanRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.request.SearchCuocTroChuyenRequest;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.ChatRealtimeResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.dto.response.CuocTroChuyenProjection;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.CuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.entity.TinNhan;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.enums.LoaiCuocTroChuyen;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.mapper.mapstruct.TinNhanMapper;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.page.CursorPageResponse;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.repository.CuocTroChuyenRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.repository.TinNhanRepository;
import com.fourspicehotpot.backend.modules.cuoctrochuyen.service.CuocTroChuyenService;
import com.fourspicehotpot.backend.modules.nhanvien.entity.NhanVien;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuocTroChuyenServiceImpl implements CuocTroChuyenService {
    private final CuocTroChuyenRepository cuocTroChuyenRepository;
    private final TinNhanRepository tinNhanRepository;
    private final WebSocketService webSocketService;
    private final TinNhanMapper tinNhanMapper;

    @Override
    public ApiResponseCommon<CursorPageResponse<CuocTroChuyenProjection>> getAllCuocTroChuyen(SearchCuocTroChuyenRequest request) {
        int limit = CursorPageConstants.DEFAULT_PAGE_SIZE;

        List<CuocTroChuyenProjection> list = cuocTroChuyenRepository.getDanhSachCuocTroChuyen(
                request.getTenKhachHang(),
                request.getCursor(),
                limit + 1
        );

        boolean hasNext = list.size() > limit;
        if (hasNext) list = list.subList(0, limit);

        LocalDateTime nextCursor = hasNext
                ? list.get(list.size() - 1).getThoiGianTinNhanCuoi()
                : null;

        return ApiResponseCommon.success(
                CursorPageResponse.<CuocTroChuyenProjection>builder()
                        .data(list)
                        .nextCursor(nextCursor)
                        .hasNext(hasNext)
                        .build(),
                HttpStatus.OK,
                "Thành công"
        );
    }

    @Override
    @Transactional
    public ApiResponseCommon<ChatRealtimeResponse> replyTinNhan(ReplyTinNhanRequest request, Long idNhanVien) {

        CuocTroChuyen cuocTroChuyen = cuocTroChuyenRepository.findById(request.getIdCuocTroChuyen())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        TinNhan tinNhan = TinNhan.builder()
                .cuocTroChuyen(cuocTroChuyen)
                .idNguoiGui(idNhanVien)
                .noiDung(request.getNoiDung())
                .loaiNguoiGui("NHAN_VIEN")
                .loaiTinNhan("TEXT")
                .daDoc(true)
                .build();

        tinNhanRepository.save(tinNhan);

        ChatRealtimeResponse response = tinNhanMapper.toResponse(tinNhan);
        webSocketService.sendChatToTopic("/topic/chat", response);

        return ApiResponseCommon.success(response, HttpStatus.OK, "Thành công");
    }

    @Override
    @Transactional
    public ApiResponseCommon<Void> nhanXuLy(Long idCuocTroChuyen, Long idNhanVien) {
        CuocTroChuyen cuocTroChuyen = cuocTroChuyenRepository.findById(idCuocTroChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        cuocTroChuyen.setLoai(LoaiCuocTroChuyen.NHAN_VIEN.name());
        cuocTroChuyen.setNhanVienXuLy(NhanVien.builder().id(idNhanVien).build());
        cuocTroChuyenRepository.save(cuocTroChuyen);

        // Thông báo realtime cho khách biết nhân viên đã vào
        webSocketService.sendChatToTopic("/topic/chat/" + idCuocTroChuyen,
                ChatRealtimeResponse.builder()
                        .loaiNguoiGui("SYSTEM")
                        .noiDung("Nhân viên đã tham gia cuộc trò chuyện!")
                        .build()
        );

        return ApiResponseCommon.success(null, HttpStatus.OK, "Thành công");
    }

    @Override
    @Transactional
    public ApiResponseCommon<Void> ketThucXuLy(Long idCuocTroChuyen) {
        CuocTroChuyen cuocTroChuyen = cuocTroChuyenRepository.findById(idCuocTroChuyen)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy cuộc trò chuyện"));

        cuocTroChuyen.setLoai(LoaiCuocTroChuyen.AI.name());
        cuocTroChuyen.setNhanVienXuLy(null);
        cuocTroChuyenRepository.save(cuocTroChuyen);

        return ApiResponseCommon.success(null, HttpStatus.OK, "Thành công");
    }
}
